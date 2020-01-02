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
      setState(() {
        body = Scaffold(
          body: Wrap(children: [
            Text(json['title'], style: theme.textTheme.title, softWrap: true),
            Text(json['date'], style: theme.textTheme.title, softWrap: true),
            Text(json['species']['name'], style: theme.textTheme.body1, softWrap: true),
            Text(json['family']['name'], style: theme.textTheme.body1, softWrap: true),
            Text(json['genus']['name'], style: theme.textTheme.body1, softWrap: true),
            Text(json['text'], style: theme.textTheme.body1, softWrap: true)
          ]),
        );
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
