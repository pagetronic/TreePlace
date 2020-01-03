import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class SpecimensMap extends StatefulWidget {
  SpecimensMap({Key key}) : super(key: key);

  @override
  State<SpecimensMap> createState() => SpecimensMapState();
}

class SpecimensMapState extends State<SpecimensMap> {
  Completer<GoogleMapController> _controller = Completer();

  Set<Marker> markers = Set<Marker>();

  @override
  Widget build(BuildContext context) {
    final dynamic json = ModalRoute.of(context).settings.arguments;

    List<dynamic> coordinates = json['location']['coordinates'];

    CameraPosition kspecimen = CameraPosition(
      target: LatLng(coordinates[1], coordinates[0]),
      zoom: 14.4746,
    );

    return new Scaffold(
      body: GoogleMap(
        markers: markers,
        mapType: MapType.hybrid,
        initialCameraPosition: kspecimen,
        onMapCreated: (GoogleMapController controller) {
          _controller.complete(controller);
          setMarker(LatLng(coordinates[1], coordinates[0]), json);
        },
      ),
    );
  }

  BitmapDescriptor markerBitmap = null;

  void setMarker(LatLng latLng, dynamic json) async {
    if (markerBitmap == null) {
      markerBitmap = BitmapDescriptor.fromBytes(
          await getBytesFromAsset('assets/marker.png', 64));
    }
    Marker marker = Marker(
      icon: markerBitmap,
      markerId: new MarkerId("xxxxx"),
      position: latLng,
      onTap: () {
        Navigator.pushNamed(context, '/specimens/view',
            arguments: {'id': json['id'], 'title': json['title']});
      },
    );
    setState(() {
      markers.add(marker);
    });
  }

  Future<Uint8List> getBytesFromAsset(String path, int width) async {
    ByteData data = await rootBundle.load(path);
    ui.Codec codec = await ui.instantiateImageCodec(data.buffer.asUint8List(),
        targetWidth: width);
    ui.FrameInfo fi = await codec.getNextFrame();
    return (await fi.image.toByteData(format: ui.ImageByteFormat.png))
        .buffer
        .asUint8List();
  }
}
