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
        var result = (key != null) ? json[key] : json;
        state.update(result, json);
      }, error: (code, json) {
        print(json.toString());
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
  dynamic base;
  List<dynamic> result = [];
  String paging = "";

  Widget Function(dynamic) builder;
  Function(String) getNext;
  Function(dynamic) first;
  bool loading = false;

  ListApiState(Widget Function(dynamic) builder, Function(String) getNext,
      {Function(dynamic) first}) {
    this.builder = builder;
    this.getNext = (paging) {
      loading = true;
      getNext(paging);
    };
    this.first = first;
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
        itemCount: result.length +
            (paging != null || result.length == 0
                ? 1
                : (first != null ? 1 : 0)),
        itemBuilder: (context, index) {
          if (result.length == 0 && paging == "") {
            getNext(null);
            paging = null;
          } else if (paging != null &&
              index == Math.max(0, result.length - 1)) {
            getNext(paging);
            paging = null;
          } else if (first != null && index == 0 && base != null) {
            return first(base);
          }

          if (result.length == 0 && !loading) {
            return first == null
                ? new Center(child: Text("Empty"))
                : new Center();
          }
          if (index >= result.length || (result.length == 0 && loading)) {
            return new Center(
                child: CircularProgressIndicator(), heightFactor: 3.5);
          }
          return builder(result[index]);
        });
  }

  void update(json, base) {
    var next = json['paging'] != null ? json['paging']['next'] : null;
    var result_ = json['result'] != null ? json['result'] : [];
    if (!mounted) {
      loading = false;
      this.base = base;
      paging = next;
      this.result.addAll(result_);
      return;
    }
    setState(() {
      loading = false;
      this.base = base;
      paging = next;
      this.result.addAll(result_);
    });
  }
}
