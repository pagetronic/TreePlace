import 'package:agroneo_treeplace/api/list.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class ForumsViews extends StatefulWidget {
  final String title;

  ForumsViews({Key key, this.title}) : super(key: key);

  @override
  _ForumsState createState() => _ForumsState();
}

class _ForumsState extends State<ForumsViews> {
  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);
    return Scaffold(
        body: ListApi.get('/questions', (dynamic json) {
      return threadTile(theme, title: json['title'], text: json['text']);
    }, key: 'threads', params: {'lng': 'fr'}));
  }
}

threadTile(theme, {String title, String text}) {
  List<Widget> childrens = [];

  if (title != null) {
    childrens.add(Text(title, style: theme.textTheme.title, softWrap: true));
  }
  if (text != null) {
    childrens.add(Text(text, style: theme.textTheme.body1, softWrap: true));
  }
  return Wrap(spacing: 8.0, runSpacing: 4.0, children: childrens);
}
