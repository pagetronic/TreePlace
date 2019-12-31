import 'package:agroneo_treeplace/api/list.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:html_unescape/html_unescape.dart';

class ThreadView extends StatefulWidget {
  ThreadView({Key key}) : super(key: key);

  @override
  ThreadViewState createState() => ThreadViewState();
}

class ThreadViewState extends BaseState<ThreadView> {
  @override
  Widget build(BuildContext context) {
    final dynamic args = ModalRoute.of(context).settings.arguments;

    var theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(args['title']),
        actions: <Widget>[connector],
      ),
      body: ListApi.get("/threads/" + args['id'], (json) {
        return postTile(context, theme,
            id: json['id'],
            title: json['title'],
            text: json['text'],
            user: json['user']);
      }, first: (json) {
        return postTile(context, theme,
            id: json['id'],
            title: json['title'],
            text: json['text'],
            user: json['user']);
      }, key: 'posts'),
    );
  }
}

postTile(context, theme, {String id, String title, String text, dynamic user}) {
  var unescape = new HtmlUnescape();
  var logo = (user != null && user['avatar'] != null) ? user['avatar'] : null;
  if (title == null) title = '';
  if (text == null) text = '';
  return Card(
      child: ListTile(
    leading: Image.network(logo + '@40x40'),
    title: Text(unescape.convert(title)),
    subtitle: Text(unescape.convert(text)),
  ));
}
