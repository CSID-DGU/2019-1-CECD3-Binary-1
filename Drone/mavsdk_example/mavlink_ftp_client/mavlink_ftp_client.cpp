/**
 * @file mavlink_ftp.cpp
 *
 * @brief Demonstrates how to use Mavlink FTP client with the MAVSDK.
 *
 * @author Matej Frančeškin <matej@auterion.com>,
 * @date 2019-09-06
 */

#include <mavsdk/mavsdk.h>
#include <mavsdk/plugins/mavlink_ftp/mavlink_ftp.h>

#include <functional>
#include <future>
#include <iostream>
#include <iomanip>
#include <cstring>

#define ERROR_CONSOLE_TEXT "\033[31m" // Turn text on console red
#define NORMAL_CONSOLE_TEXT "\033[0m" // Restore normal console colour

using namespace mavsdk;

void usage(const std::string& bin_name)
{
    std::cout << NORMAL_CONSOLE_TEXT << "Usage : " << bin_name
              << " <connection_url> <server component id> <command> <parameters>" << std::endl
              << std::endl
              << "Connection URL format should be :" << std::endl
              << " For TCP : tcp://[server_host][:server_port]" << std::endl
              << " For UDP : udp://[bind_host][:bind_port]" << std::endl
              << " For Serial : serial:///path/to/serial/dev[:baudrate]" << std::endl
              << "For example, to connect to the simulator use URL: udp://:14540" << std::endl
              << std::endl
              << "Server component id is for example 1 for autopilot or 240 for companion computer."
              << std::endl
              << std::endl
              << "Supported commands :" << std::endl
              << " put <file> <path>    : Upload one file to remote directory" << std::endl
              << " get <file> <path>    : Download remote file to local directory" << std::endl
              << " delete <file>        : Delete remote file" << std::endl
              << " rename <old> <new>   : Rename file" << std::endl
              << " dir <path>           : List contents of remote directory" << std::endl
              << " mkdir <path>         : Make directory on remote machine" << std::endl
              << " rmdir [-r] <path>    : Remove directory on remote machine. [-r] recursively"
              << std::endl
              << " cmp <local> <remote> : Compare local and remote file" << std::endl
              << " crc32 <path>         : Get remote file crc32" << std::endl
              << " localcrc32 <path>    : Get local file crc32" << std::endl
              << std::endl
              << "Return codes:" << std::endl
              << " 0 : Success" << std::endl
              << " 1 : Failure" << std::endl
              << " 2 : File does not exist" << std::endl
              << " 3 : Files are different (cmp command)" << std::endl;
}

MavlinkFTP::Result reset_server(std::shared_ptr<MavlinkFTP>& mavlink_ftp)
{
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->reset_async([prom](MavlinkFTP::Result result) { prom->set_value(result); });
    return future_result.get();
}

MavlinkFTP::Result
create_directory(std::shared_ptr<MavlinkFTP>& mavlink_ftp, const std::string& path)
{
    std::cout << "Creating directory: " << path << std::endl;
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->create_directory_async(
        path, [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result remove_file(std::shared_ptr<MavlinkFTP>& mavlink_ftp, const std::string& path)
{
    std::cout << "Removing file: " << path << std::endl;
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->remove_file_async(
        path, [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result remove_directory(
    std::shared_ptr<MavlinkFTP>& mavlink_ftp, const std::string& path, bool recursive = true)
{
    if (recursive) {
        auto prom = std::make_shared<
            std::promise<std::pair<MavlinkFTP::Result, std::vector<std::string>>>>();
        auto future_result = prom->get_future();
        mavlink_ftp->list_directory_async(
            path, [prom](MavlinkFTP::Result result, std::vector<std::string> list) {
                prom->set_value(
                    std::pair<MavlinkFTP::Result, std::vector<std::string>>(result, list));
            });

        std::pair<MavlinkFTP::Result, std::vector<std::string>> result = future_result.get();
        if (result.first == MavlinkFTP::Result::SUCCESS) {
            for (auto entry : result.second) {
                if (entry[0] == 'D') {
                    remove_directory(mavlink_ftp, path + "/" + entry.substr(1, entry.size() - 1));
                } else if (entry[0] == 'F') {
                    auto i = entry.find('\t');
                    std::string name = entry.substr(1, i - 1);
                    remove_file(mavlink_ftp, path + "/" + name);
                }
            }
        }
    }
    std::cout << "Removing dir:  " << path << std::endl;

    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->remove_directory_async(
        path, [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result list_directory(std::shared_ptr<MavlinkFTP>& mavlink_ftp, const std::string& path)
{
    std::cout << "List directory: " << path << std::endl;
    auto prom =
        std::make_shared<std::promise<std::pair<MavlinkFTP::Result, std::vector<std::string>>>>();
    auto future_result = prom->get_future();
    mavlink_ftp->list_directory_async(
        path, [prom](MavlinkFTP::Result result, std::vector<std::string> list) {
            prom->set_value(std::pair<MavlinkFTP::Result, std::vector<std::string>>(result, list));
        });

    std::pair<MavlinkFTP::Result, std::vector<std::string>> result = future_result.get();
    if (result.first == MavlinkFTP::Result::SUCCESS) {
        for (auto entry : result.second) {
            std::cout << entry << std::endl;
        }
    }
    return result.first;
}

MavlinkFTP::Result download_file(
    std::shared_ptr<MavlinkFTP>& mavlink_ftp,
    const std::string& remote_file_path,
    const std::string& local_path)
{
    std::cout << "Download file: " << remote_file_path << " to " << local_path << std::endl;
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->download_async(
        remote_file_path,
        local_path,
        [](uint32_t bytes_transferred, uint32_t file_size) {
            int percentage = (file_size > 0) ? bytes_transferred * 100 / file_size : 0;
            std::cout << NORMAL_CONSOLE_TEXT << "\rDownloading [" << std::setw(3) << percentage
                      << "%] " << bytes_transferred << " of " << file_size;
            if (bytes_transferred == file_size) {
                std::cout << std::endl;
            }
        },
        [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result upload_file(
    std::shared_ptr<MavlinkFTP>& mavlink_ftp,
    const std::string& local_file_path,
    const std::string& remote_path)
{
    std::cout << "Upload file: " << local_file_path << " to " << remote_path << std::endl;
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->upload_async(
        local_file_path,
        remote_path,
        [](uint32_t bytes_transferred, uint32_t file_size) {
            int percentage = (file_size > 0) ? bytes_transferred * 100 / file_size : 0;
            std::cout << NORMAL_CONSOLE_TEXT << "\rUploading [" << std::setw(3) << percentage
                      << "%] " << bytes_transferred << " of " << file_size;
            if (bytes_transferred == file_size) {
                std::cout << std::endl;
            }
        },
        [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result rename_file(
    std::shared_ptr<MavlinkFTP>& mavlink_ftp,
    const std::string& old_name,
    const std::string& new_name)
{
    std::cout << "Rename file: " << old_name << " to " << new_name << std::endl;
    auto prom = std::make_shared<std::promise<MavlinkFTP::Result>>();
    auto future_result = prom->get_future();
    mavlink_ftp->rename_async(
        old_name, new_name, [prom](MavlinkFTP::Result result) { prom->set_value(result); });

    return future_result.get();
}

MavlinkFTP::Result get_file_checksum(
    std::shared_ptr<MavlinkFTP>& mavlink_ftp, const std::string& remote_file_path, uint32_t& crc)
{
    auto prom = std::make_shared<std::promise<std::pair<MavlinkFTP::Result, uint32_t>>>();

    auto future_result = prom->get_future();
    mavlink_ftp->calc_file_crc32_async(
        remote_file_path, [prom](MavlinkFTP::Result result, uint32_t checksum) {
            prom->set_value(std::pair<MavlinkFTP::Result, uint32_t>(result, checksum));
        });

    std::pair<MavlinkFTP::Result, uint32_t> result = future_result.get();
    if (result.first == MavlinkFTP::Result::SUCCESS) {
        crc = result.second;
    }
    return result.first;
}

int main(int argc, char** argv)
{
    Mavsdk mavsdk;

    auto prom = std::make_shared<std::promise<void>>();
    auto future_result = prom->get_future();

    std::cout << NORMAL_CONSOLE_TEXT << "Waiting to discover system..." << std::endl;
    mavsdk.register_on_discover([prom](uint64_t uuid) {
        std::cout << "Discovered system with UUID: " << uuid << std::endl;
        prom->set_value();
    });

    std::string connection_url;
    ConnectionResult connection_result;

    if (argc >= 4) {
        connection_url = argv[1];
        connection_result = mavsdk.add_any_connection(connection_url);
    } else {
        usage(argv[0]);
        return 1;
    }

    if (connection_result != ConnectionResult::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT
                  << "Connection failed: " << connection_result_str(connection_result)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    auto status = future_result.wait_for(std::chrono::seconds(5));
    if (status == std::future_status::timeout) {
        std::cout << "Timeout waiting for connection." << std::endl;
        return 1;
    }

    future_result.get();

    System& system = mavsdk.system();
    auto mavlink_ftp = std::make_shared<MavlinkFTP>(system);
    mavlink_ftp->set_timeout(200);
    mavlink_ftp->set_retries(10);
    try {
        mavlink_ftp->set_target_component_id(std::stoi(argv[2]));
    } catch (...) {
        std::cout << ERROR_CONSOLE_TEXT << "Invalid argument: " << argv[2] << NORMAL_CONSOLE_TEXT
                  << std::endl;
        return 1;
    }

    MavlinkFTP::Result res;
    res = reset_server(mavlink_ftp);
    if (res != MavlinkFTP::Result::SUCCESS) {
        std::cout << ERROR_CONSOLE_TEXT << "Reset server error: " << mavlink_ftp->result_str(res)
                  << NORMAL_CONSOLE_TEXT << std::endl;
        return 1;
    }

    std::string command = argv[3];

    if (command == "put") {
        if (argc < 6) {
            usage(argv[0]);
            return 1;
        }
        res = upload_file(mavlink_ftp, argv[4], argv[5]);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "File uploaded." << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT << "File upload error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
    } else if (command == "get") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        res = download_file(mavlink_ftp, argv[4], (argc == 6) ? argv[5] : ".");
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "File downloaded." << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "File download error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
    } else if (command == "rename") {
        if (argc < 6) {
            usage(argv[0]);
            return 1;
        }
        res = rename_file(mavlink_ftp, argv[4], argv[5]);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "File renamed." << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT << "File rename error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
    } else if (command == "mkdir") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        res = create_directory(mavlink_ftp, argv[4]);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "Directory created." << std::endl;
        } else if (res == MavlinkFTP::Result::FILE_EXISTS) {
            std::cout << "Directory already exists." << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Create directory error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return 1;
        }
    } else if (command == "rmdir") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        std::string path = argv[4];
        bool recursive = false;
        if (argc == 6) {
            if (path == "-r") {
                recursive = true;
                path = argv[5];
            } else if (std::string(argv[5]) == "-r") {
                recursive = true;
            }
        }
        res = remove_directory(mavlink_ftp, path, recursive);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "Directory removed." << std::endl;
        } else if (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) {
            std::cout << "Directory does not exist." << std::endl;
            return 2;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Remove directory error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return 1;
        }
    } else if (command == "dir") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        res = list_directory(mavlink_ftp, argv[4]);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "Directory listed." << std::endl;
        } else if (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) {
            std::cout << "Directory does not exist." << std::endl;
            return 2;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "List directory error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return 1;
        }
    } else if (command == "delete") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        res = remove_file(mavlink_ftp, argv[4]);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "File deleted." << std::endl;
        } else if (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) {
            std::cout << "File does not exist." << std::endl;
            return 2;
        } else {
            std::cout << ERROR_CONSOLE_TEXT << "Delete file error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return 1;
        }
    } else if (command == "crc32") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        uint32_t crc32;
        res = get_file_checksum(mavlink_ftp, argv[4], crc32);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "CRC32=" << crc32 << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Get file crc32 error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
    } else if (command == "localcrc32") {
        if (argc < 5) {
            usage(argv[0]);
            return 1;
        }
        uint32_t crc32;
        res = mavlink_ftp->calc_local_file_crc32(argv[4], crc32);
        if (res == MavlinkFTP::Result::SUCCESS) {
            std::cout << "Local CRC32=" << crc32 << std::endl;
        } else {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Get local file crc32 error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
    } else if (command == "cmp") {
        if (argc < 6) {
            usage(argv[0]);
            return 1;
        }
        uint32_t crc32_loc;
        res = mavlink_ftp->calc_local_file_crc32(argv[4], crc32_loc);
        if (res != MavlinkFTP::Result::SUCCESS) {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Get local file crc32 error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
        uint32_t crc32_rem;
        res = get_file_checksum(mavlink_ftp, argv[5], crc32_rem);
        if (res != MavlinkFTP::Result::SUCCESS) {
            std::cout << ERROR_CONSOLE_TEXT
                      << "Get file crc32 error: " << mavlink_ftp->result_str(res)
                      << NORMAL_CONSOLE_TEXT << std::endl;
            return (res == MavlinkFTP::Result::FILE_DOES_NOT_EXIST) ? 2 : 1;
        }
        if (crc32_loc == crc32_rem) {
            std::cout << "Files are equal" << std::endl;
        } else {
            std::cout << "Files are different" << std::endl;
            return 3;
        }
    } else {
        std::cout << ERROR_CONSOLE_TEXT << "Unknown command: " << command << NORMAL_CONSOLE_TEXT
                  << std::endl;
        return 1;
    }

    return 0;
}
