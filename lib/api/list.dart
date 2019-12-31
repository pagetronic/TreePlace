import 'dart:math' as Math;

import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ListApi extends StatefulWidget {
  ListApiState state;

  ListApi(this.state);

  static ListApi get(String url, Widget Function(dynamic) builder,
      {Map<String, String> params, String key}) {
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
        state.update(result);
      }, error: (code, json) {
        print(json.toString());
      });
    });
    return new ListApi(state);
  }

  @override
  ListApiState createState() {
    return state;
  }
}

class ListApiState extends BaseState<ListApi> {
  List<dynamic> result = [];
  String paging = "";

  Widget Function(dynamic) builder;
  Function(String) getNext;

  ListApiState(Widget Function(dynamic) builder, Function(String) getNext) {
    this.builder = builder;
    this.getNext = getNext;
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
        itemCount:
            result.length + (paging != null || result.length == 0 ? 1 : 0),
        itemBuilder: (context, index) {
          if (result.length == 0 && paging == "") {
            getNext(null);
            paging = null;
          } else if (paging != null &&
              index == Math.max(0, result.length - 1)) {
            getNext(paging);
            paging = null;
          }
          if (index >= result.length) {
            return new Center(
                child: CircularProgressIndicator(), heightFactor: 3.5);
          } else {
            return builder(result[index]);
          }
        });
  }

  void update(json) {
    var next = json['paging'] != null ? json['paging']['next'] : null;
    var result_ = json['result'] != null ? json['result'] : [];
    if (!mounted) {
      paging = next;
      result.addAll(result_);
      return;
    }
    setState(() {
      paging = next;
      result.addAll(result_);
    });
  }
}
