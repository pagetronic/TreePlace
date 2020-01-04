import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:agroneo_treeplace/api/api.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class SpecimensMap extends StatefulWidget {
  SpecimensMap({Key key}) : super(key: key);

  @override
  State<SpecimensMap> createState() => SpecimensMapState();
}

class SpecimensMapState extends State<SpecimensMap> {
  GoogleMapController _controller;

  Set<Marker> markers = Set<Marker>();
  Set<Polygon> polygons = Set<Polygon>();
  GoogleMap map;
  int zoom = 14;

  @override
  Widget build(BuildContext context) {
    final dynamic json = ModalRoute.of(context).settings.arguments;

    List<dynamic> coordinates = json['location']['coordinates'];

    CameraPosition kspecimen = CameraPosition(
      target: LatLng(coordinates[1], coordinates[0]),
      zoom: zoom.toDouble(),
    );

    map = GoogleMap(
        markers: markers,
        polygons: polygons,
        mapType: MapType.hybrid,
        initialCameraPosition: kspecimen,
        onMapCreated: (GoogleMapController controller) {
          _controller = controller;
          setMarker(LatLng(coordinates[1], coordinates[0]), json);
        },
        onCameraMove: (CameraPosition position) {
          this.zoom = position.zoom.toInt();
        },
        onCameraIdle: () {
          updateSpecimens();
        });

    return new Scaffold(
      body: map,
    );
  }

  void setMarker(LatLng latLng, dynamic json) async {
    BitmapDescriptor markerBitmap = BitmapDescriptor.fromBytes(
        await getBytesFromAsset('assets/marker.png', 64));

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

  void updateSpecimens() async {
    BitmapDescriptor markerBitmap = BitmapDescriptor.fromBytes(
        await getBytesFromAsset('assets/marker.png', 64));
    BitmapDescriptor clusterBitmap = BitmapDescriptor.fromBytes(
        await getBytesFromAsset('assets/cluster.png', 64));

    LatLngBounds bounds = await _controller.getVisibleRegion();
    ApiRequest.post("/gaia", {
      'action': 'specimens',
      'bounds': {
        'south': bounds.southwest.latitude,
        'west': bounds.southwest.longitude,
        'north': bounds.northeast.latitude,
        'east': bounds.northeast.longitude,
      },
      'zoom': zoom,
      'lng': 'fr'
    }, error: (code, json) {
      print(json);
    }, success: (json) {
      Set<Marker> markers = Set<Marker>();
      Set<Polygon> polygons = Set<Polygon>();
      for (var item in json['result']) {
        if (item['specimens'] != null) {
          for (final specimen in item['specimens']) {
            List<dynamic> coordinates = specimen['location']['coordinates'];
            Marker marker = Marker(
              icon: markerBitmap,
              markerId: new MarkerId(specimen['id']),
              position: LatLng(coordinates[1], coordinates[0]),
              onTap: () {
                Navigator.pushNamed(context, '/specimens/view', arguments: {
                  'id': specimen['id'],
                  'title': specimen['title']
                });
              },
            );
            markers.add(marker);
          }
        }
        if (item['count'] != null) {
          List<LatLng> points = [];
          for (List polycoord in item['location']['coordinates']) {
            for (List coord in polycoord) {
              points.add(LatLng(coord[1], coord[0]));
            }

            Polygon polygon =
                Polygon(polygonId: new PolygonId('xx'), points: points);
            polygons.add(polygon);
          }
        }
      }
      setState(() {
        this.markers = markers;
        this.polygons = polygons;
      });
    });
  }
}
