import 'dart:convert';

import 'package:http/http.dart' as http;

import '../settings.dart' as settings;
import 'oauth.dart';

class ApiRequest {
  //Get method to Api
  static ApiRequest get(String url, {Map<String, String> params}) {
    ApiRequest apiRequest = new ApiRequest();
    apiRequest._get(url, params: params);
    return apiRequest;
  }

  //Post method to Api
  static ApiRequest post(String url, dynamic data) {
    ApiRequest apiRequest = new ApiRequest();
    apiRequest._post(url, data);
    return apiRequest;
  }

  //Function to execute on success
  ApiRequest success(Function(dynamic rez) onSuccess) {
    this._onSuccess = onSuccess;
    return this;
  }

  //Function to execute on error
  ApiRequest error(Function(int code, dynamic rez) onError) {
    this._onError = onError;
    return this;
  }

  bool _aborted = false;

  //Abort request to Api
  void abort() {
    this._aborted = true;
  }

  Function(dynamic rez) _onSuccess = (dynamic rez) {};

  Function(int code, dynamic rez) _onError = (int code, dynamic rez) {};

  Future _get(String url, {Map<String, String> params, int retry}) async {
    if (_aborted) {
      return;
    }
    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      _onError(-1, {'error': 'Too many retry'});
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
    if (_aborted) {
      return;
    }
    http.Response response =
        await http.get(settings.apiUrl + url + query, headers: headers);

    if (_aborted) {
      return;
    }

    int statusCode = response.statusCode;
    dynamic rez = json.decode(response.body);

    if (_aborted) {
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      retry--;
      return await _get(url, params: params, retry: retry);
    }

    if (_aborted) {
      return;
    }
    if (statusCode != 200) {
      _onError(statusCode, rez);
    } else {
      _onSuccess(rez);
    }
  }

  Future _post(String url, dynamic data, {int retry}) async {
    if (_aborted) {
      return;
    }

    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      _onError(-1, {'error': 'Too many retry'});
      return;
    }

    String jsonBody = json.encode(data);
    final encoding = Encoding.getByName('utf-8');
    var headers = await _getHeaders();
    if (_aborted) {
      return;
    }
    headers.putIfAbsent('Content-Type', () => 'application/json');
    http.Response response = await http.post(
      settings.apiUrl + url,
      headers: headers,
      body: jsonBody,
      encoding: encoding,
    );

    if (_aborted) {
      return;
    }
    int statusCode = response.statusCode;
    dynamic rez = json.decode(response.body);
    if (_aborted) {
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      retry--;
      await _post(url, data, retry: retry);
      return;
    }
    if (_aborted) {
      return;
    }
    if (statusCode != 200) {
      _onError(statusCode, rez);
    } else {
      _onSuccess(rez);
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
