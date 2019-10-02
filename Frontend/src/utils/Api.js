import axios from 'axios';

const server = 'http://localhost:8080';

const GET = (uri, param) => {
  return axios.get(server + uri, {
    params: param
  });
};

const POST = (uri, data) => {
  return axios.post(server + uri, { data });
};

const PUT = (uri, data) => {
  return axios.put(server + uri, { data });
};

const DELETE = (uri) => {
  return axios.delete(server + uri);
};

export {
  GET,
  POST,
  PUT,
  DELETE
};