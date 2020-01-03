import 'dart:math' as Math;

import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ListApi extends StatefulWidget {
  ListApiState state;

  ListApi(this.state);

  static ListApi get(String url, Widget Function(dynamic) builder,
      {Function(dynamic) first, Map<String, String> params, String key}) {
    ListApiState state;
    state = new ListApiState(builder, (paging) {
      if (params == null) {
        params = new Map<String, String>();
      }
      if (paging != null) {
        params.addAll({"paging": paging});
      }
      ApiRequest.get(url, params: params, success: (json) {
        state.update(json, key);
      }, error: (code, json) {
        state.error();
      });
    }, first: first);
    return new ListApi(state);
  }

  @override
  ListApiState createState() {
    return state;
  }
}

class ListApiState extends BaseState<ListApi> {
  List<dynamic> result = [];
  int scrolldist = 0;

  Widget Function(dynamic) builder;
  Function(String) getNext;
  Function(dynamic) first;
  bool loading = true;

  ListApiState(Widget Function(dynamic) builder, Function(String) getNext,
      {Function(dynamic) first}) {
    this.builder = builder;
    this.getNext = (paging) {
      loading = true;
      getNext(paging);
    };
    this.first = first;
    getNext(null);
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
        itemCount: result.length +
            (result.length > 0 && result.last['@next'] != null || loading
                ? 1
                : 0),
        itemBuilder: (context, index) {
          if (index == Math.max(0, result.length - scrolldist) &&
              result.length > 0 &&
              result.last['@next'] != null) {
            getNext(result.last['@next']);
          }

          if (index >= result.length && loading) {
            return Center(
                child: CircularProgressIndicator(), heightFactor: 3.5);
          }

          if (result.length == 0 && !loading) {
            return first == null ? Center(child: Text("Empty")) : Center();
          }

          return (first != null && index == 0)
              ? first(result[0])
              : builder(result[index]);
        });
  }

  void error() {
    setState(() {
      loading = false;
    });
  }

  void update(dynamic json_, String key) {
    var json = (key != null) ? json_[key] : json_;

    if (result.length == 0 && first != null) {
      if (key != null) {
        json_.remove(key);
      }
      result.add(json_);
    }

    List<dynamic> result_ = json['result'] != null ? json['result'] : [];
    int dist = 0;
    if (json['paging'] != null) {
      dynamic paging = json['paging'];
      dist = (paging['limit'] / 3).round();
      if (paging['next'] != null) {
        result_.last['@next'] = paging['next'];
      }
      if (paging['prev'] != null) {
        result_.first['@prev'] = paging['prev'];
      }
    }

    if (mounted) {
      setState(() {
        loading = false;
        scrolldist = dist;
        result.addAll(result_);
      });
    }
  }
}
