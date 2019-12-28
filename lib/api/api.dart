import 'dart:convert';

import 'package:http/http.dart' as http;

import '../settings.dart' as settings;
import 'oauth.dart';

class ApiRequest {
  bool aborted = false;

  bool isAbort() {
    return aborted;
  }

  void abort() {
    aborted = true;
  }

  Function(dynamic rez) _onSuccess = (dynamic rez) {};

  ApiRequest success(Function(dynamic rez) onSuccess) {
    this._onSuccess = onSuccess;
    return this;
  }

  Function(int code, dynamic rez) _onError = (int code, dynamic rez) {};

  ApiRequest error(Function(int code, dynamic rez) onError) {
    this._onError = onError;
    return this;
  }

  static ApiRequest get(String url, {Map<String, String> params}) {
    ApiRequest apiRequest = new ApiRequest();
    _get(url, params: params, apiRequest: apiRequest);
    return apiRequest;
  }

  static ApiRequest post(String url, dynamic data) {
    ApiRequest apiRequest = new ApiRequest();
    _post(url, data, apiRequest: apiRequest);
    return apiRequest;
  }

  static Future _get(String url,
      {Map<String, String> params, ApiRequest apiRequest, int retry}) async {
    if (apiRequest.isAbort()) {
      return;
    }
    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      apiRequest._onError(-1, {'error': 'Too many retry'});
      return;
    }
    String query = "";
    if (params != null) {
      Iterator<MapEntry<String, String>> it = params.entries.iterator;
      String sep = "?";
      while (it.moveNext()) {
        query += sep + it.current.key + "=" + it.current.value;
        sep = "&";
      }
    }

    var headers = await _getHeaders();
    if (apiRequest.isAbort()) {
      return;
    }
    http.Response response =
        await http.get(settings.apiUrl + url + query, headers: headers);

    if (apiRequest.isAbort()) {
      return;
    }

    int statusCode = response.statusCode;
    dynamic rez = json.decode(response.body);

    if (apiRequest.isAbort()) {
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      retry--;
      return await _get(url,
          params: params, apiRequest: apiRequest, retry: retry);
    }

    if (apiRequest.isAbort()) {
      return;
    }
    if (statusCode != 200) {
      apiRequest._onError(statusCode, rez);
    } else {
      apiRequest._onSuccess(rez);
    }
  }

  static Future _post(String url, dynamic data,
      {ApiRequest apiRequest, int retry}) async {
    if (apiRequest.isAbort()) {
      return;
    }

    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      apiRequest._onError(-1, {'error': 'Too many retry'});
      return;
    }

    String jsonBody = json.encode(data);
    final encoding = Encoding.getByName('utf-8');
    var headers = await _getHeaders();
    if (apiRequest.isAbort()) {
      return;
    }
    headers.putIfAbsent('Content-Type', () => 'application/json');
    http.Response response = await http.post(
      settings.apiUrl + url,
      headers: headers,
      body: jsonBody,
      encoding: encoding,
    );

    if (apiRequest.isAbort()) {
      return;
    }
    int statusCode = response.statusCode;
    dynamic rez = json.decode(response.body);
    if (apiRequest.isAbort()) {
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      retry--;
      await _post(url, data, apiRequest: apiRequest, retry: retry);
      return;
    }
    if (apiRequest.isAbort()) {
      return;
    }
    if (statusCode != 200) {
      apiRequest._onError(statusCode, rez);
    } else {
      apiRequest._onSuccess(rez);
    }
  }

  static Future<dynamic> _getHeaders() async {
    var headers = {"User-Agent": settings.userAgent};

    String accessToken = await Oauth.getAccessToken();
    if (accessToken != null) {
      headers.putIfAbsent('Authorization', () => 'Bearer ' + accessToken);
    }
    return headers;
  }

  static Future<bool> refreshToken(int statusCode, dynamic rez) async {
    if (rez != null && rez['error'] == 'EXPIRED_ACCESS_TOKEN') {
      await Oauth.refreshToken();
      return true;
    }
    return false;
  }
}
