import 'package:agroneo_treeplace/api/api.dart';
import 'package:agroneo_treeplace/ui/widgets.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';

class SpecimenView extends StatefulWidget {
  final String title;

  SpecimenView({Key key, this.title}) : super(key: key);

  @override
  SpecimenViewState createState() => SpecimenViewState();
}

class SpecimenViewState extends BaseState<SpecimenView> {
  Widget body =
      new Center(child: CircularProgressIndicator(), heightFactor: 3.5);

  @override
  Widget build(BuildContext context) {
    final dynamic args = ModalRoute.of(context).settings.arguments;

    var theme = Theme.of(context);

    ApiRequest.get("/gaia/" + args['id'], success: (json) {
      List<Widget> childrens = [];
      if (json['images'].length > 0) {
        for (var image in json['images']) {
          childrens.add(AspectRatio(
              aspectRatio: 462.0 / 200.0,
              child: FadeInImage.assetNetwork(
                  fadeInDuration: Duration(milliseconds: 500),
                  image: image['url'] + '@462x200.jpg',
                  fit: BoxFit.cover,
                  placeholder: 'assets/arbre.png')));
        }
      }
      if (json['title'] != null) {
        childrens.add(
            Text(json['title'], style: theme.textTheme.title, softWrap: true));
      }
      if (json['family'] != null) {
        childrens.add(Text(json['family']['name'],
            style: theme.textTheme.body1, softWrap: true));
      }
      if (json['species'] != null) {
        childrens.add(Text(json['species']['name'],
            style: theme.textTheme.body1, softWrap: true));
      }
      if (json['text'] != null) {
        childrens.add(
            Text(json['text'], style: theme.textTheme.body1, softWrap: true));
      }

      setState(() {
        body = SingleChildScrollView(
            child: Column(
                children: childrens,
                crossAxisAlignment: CrossAxisAlignment.start));
      });
    });
    return Scaffold(
      appBar: AppBar(
        title: Text(args['title']),
        actions: <Widget>[connector],
      ),
      body: body,
    );
  }
}
