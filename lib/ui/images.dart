import 'package:flutter/widgets.dart';
import 'package:photo_view/photo_view.dart';

class ImageView extends StatefulWidget {
  final String title;

  ImageView({Key key, this.title}) : super(key: key);

  @override
  _ImageViewState createState() => _ImageViewState();
}

class _ImageViewState extends State<ImageView> {
  @override
  Widget build(BuildContext context) {
    final dynamic args = ModalRoute.of(context).settings.arguments;
    return Container(
        child: PhotoView(
      imageProvider: NetworkImage(args['url']),
    ));
  }
}
