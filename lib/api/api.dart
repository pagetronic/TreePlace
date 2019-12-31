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

  bool _aborted = false;

  //final callable success function
  Function(dynamic rez) _onSuccess = (dynamic rez) {};

  //final error success function
  Function(int code, dynamic rez) _onError = (int code, dynamic rez) {};

  //constructor, please prefer get() and post() functions
  ApiRequest(
      Function(dynamic rez) success, Function(int code, dynamic rez) error) {
    if (success != null) {
      _onSuccess = success;
    }
    if (error != null) {
      _onError = error;
    }
  }

  Future _get(String url, {Map<String, String> params, int retry}) async {
    if (retry == null) {
      retry = 5;
    } else if (retry == 0) {
      _onError(-1, {'error': 'Too many retry'});
      return;
    }

    String query = "";
    if (params != null && params.length > 0) {
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

    if (_aborted) {
      return;
    }

    int statusCode = response.statusCode;
    dynamic rez;
    try {
      rez = json.decode(response.body);
    } catch (Exception) {
      _onError(-1, {'error': 'json parse error'});
      return;
    }

    if (await refreshToken(statusCode, rez)) {
      retry--;
      if (_aborted) {
        return;
      }
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
      _onError(-1, {'error': 'json parse error'});
      return;
    }
    if (await refreshToken(statusCode, rez)) {
      if (_aborted) {
        return;
      }
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

  ApiRequest success(Function(dynamic rez) onSuccess) {
    this._onSuccess = onSuccess;
    return this;
  }

  ApiRequest error(Function(int statusCode, dynamic rez) onError) {
    this._onError = onError;
    return this;
  }

  static void test() {
    //Test the api GET success/error as parameter
    ApiRequest.get('/profile', success: ((rez) {
      print(rez['name']);
    }), error: ((code, rez) {
      print(rez['error']);
    }));
    //Test the api GET success/error as Future style
    ApiRequest.get('/profile').success((rez) {
      print(rez['name']);
    }).error((code, rez) {
      print(rez['error']);
    });

    //Test the api POST success/error as parameter
    ApiRequest.post('/gaia/species', {'action': 'search', 'search': 'dal'},
        success: ((rez) {
      print(rez['result'][0]['name']);
    }), error: ((code, rez) {
      print(rez['error']);
    }));
    //Test the api POST success/error as Future style
    ApiRequest.post('/gaia/species', {'action': 'search', 'search': 'dal'})
        .success((rez) {
      print(rez['result'][0]['name']);
    }).error(((code, rez) {
      print(rez['error']);
    }));
  }
}
