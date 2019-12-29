import 'dart:convert';

import 'package:http/http.dart' as http;

import '../settings.dart' as settings;
import 'oauth.dart';

class ApiRequest {
  //Get method to Api
  static ApiRequest get(String url,
      {Map<String, String> params,
      Function(dynamic rez) success,
      Function(int code, dynamic rez) error}) {
    ApiRequest apiRequest = new ApiRequest(success, error);
    apiRequest._get(url, params: params);
    return apiRequest;
  }

  //Post method to Api
  static ApiRequest post(String url, dynamic data,
      {Function(dynamic rez) success, Function(int code, dynamic rez) error}) {
    ApiRequest apiRequest = new ApiRequest(success, error);
    apiRequest._post(url, data);
    return apiRequest;
  }

  //Abort request to Api
  void abort() {
    this._aborted = true;
  }

  Function(dynamic rez) _onSuccess = (dynamic rez) {};

  Function(int code, dynamic rez) _onError = (int code, dynamic rez) {};

  ApiRequest(
      Function(dynamic rez) success, Function(int code, dynamic rez) error) {
    _onSuccess = success;
    _onError = error;
  }

  bool _aborted = false;

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
    http.Response response = await http
        .get(settings.apiUrl + url + query, headers: headers)
        .timeout(new Duration(seconds: 60));
    ;

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

    if (!_aborted && statusCode != 200) {
      _onError(statusCode, rez);
    } else if (!_aborted) {
      _onSuccess(rez);
    }
  }

  Future _post(String url, dynamic data, {int retry}) async {
    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      _onError(-1, {'error': 'Too many retry'});
      return;
    }

    var headers = await _getHeaders();
    headers.putIfAbsent('Content-Type', () => 'application/json');
    if (_aborted) {
      return;
    }
    http.Response response = await http
        .post(
          settings.apiUrl + url,
          headers: headers,
          body: json.encode(data),
          encoding: Encoding.getByName('utf-8'),
        )
        .timeout(new Duration(seconds: 60));

    if (_aborted) {
      return;
    }
    int statusCode = response.statusCode;
    dynamic rez;
    try {
      rez = json.decode(response.body);
    } catch (Exception) {
      _onError(-1, 'json parse error');
      return;
    }
    if (_aborted) {
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      retry--;
      await _post(url, data, retry: retry);
      return;
    }
    if (!_aborted && statusCode != 200) {
      _onError(statusCode, rez);
    } else if (!_aborted) {
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

  set success(Function value) {
    _onSuccess = value;
  }

  set error(Function value) {
    _onError = value;
  }
}
