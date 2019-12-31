import 'package:agroneo_treeplace/api/list.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:html_unescape/html_unescape_small.dart';

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
      return threadTile(context, theme,
          id: json['id'],
          title: json['title'],
          text: json['text'],
          user: json['user']);
    }, key: 'threads', params: {'lng': 'fr'}));
  }
}

threadTile(context, theme,
    {String id, String title, String text, dynamic user}) {
  var unescape = new HtmlUnescape();
  var logo = (user != null && user['avatar'] != null) ? user['avatar'] : null;
  if (title == null) title = '';
  if (text == null) text = '';
  return Card(
      child: ListTile(
    onTap: () {
      Navigator.pushNamed(context, '/thread', arguments: {'id': id});
    },
    leading: Image.network(logo + '@40x40'),
    title: Text(unescape.convert(title)),
    subtitle: Text(unescape.convert(text)),
  ));
}
