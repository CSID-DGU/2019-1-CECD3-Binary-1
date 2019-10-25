import axios from 'axios';

export const server = {
  id: 'BE_APP',
  url: '',
};

const GET = (uri, param) => {
  return axios.get(server.url + uri, {
    params: param
  });
};

const POST = (uri, data) => {
  return axios.post(server.url + uri, { data });
};

const PUT = (uri, data) => {
  return axios.put(server.url + uri, { data });
};

const DELETE = (uri) => {
  return axios.delete(server.url + uri);
};

export {
  GET,
  POST,
  PUT,
  DELETE
};