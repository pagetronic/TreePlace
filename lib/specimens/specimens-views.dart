import 'package:agroneo_treeplace/api/list.dart';
import 'package:agroneo_treeplace/settings.dart' as settings;
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class SpecimensViews extends StatefulWidget {
  final String title;

  SpecimensViews({Key key, this.title}) : super(key: key);

  @override
  _SpecimensViewsState createState() => _SpecimensViewsState();
}

class _SpecimensViewsState extends BaseState<SpecimensViews> {
  @override
  Widget build(BuildContext context) {
    var theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
        actions: <Widget>[connector],
      ),
      body:  ListApi.get('/gaia/specimens', (dynamic json) {
        return specimenTile(theme,
            title: json['title'], text: json['text'], images: json['images']);
      }),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.pushNamed(context, '/specimens/create');
        },
        backgroundColor: settings.secondaryColor,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}

specimenTile(theme, {String title, String text, List<dynamic> images}) {
  List<Widget> childrens = [];
  if (images.length > 0 && images[0]['url'] != null) {
    childrens.add(AspectRatio(
        aspectRatio: 462.0 / 200.0,
        child: CachedNetworkImage(
          imageUrl: images[0]['url'] + '@462x200.jpg',
          placeholder: (context, url) =>
              new Center(child: CircularProgressIndicator()),
          errorWidget: (context, url, error) => new Icon(Icons.error),
        )));
  }
  if (title != null) {
    childrens.add(Text(title, style: theme.textTheme.title, softWrap: true));
  }
  if (text != null) {
    childrens.add(Text(text, style: theme.textTheme.body1, softWrap: true));
  }
  return Wrap(spacing: 8.0, runSpacing: 4.0, children: childrens);
}
