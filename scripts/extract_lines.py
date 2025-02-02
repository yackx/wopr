# Extract lines from a geojson file

import json

def is_visible(longitude, latitude):
    return 0 <= longitude <= 48 and 30 <= latitude <= 60



def extract_coordinates(coordinates, poly_lines):
    if isinstance(coordinates[0], list):
        for coord in coordinates:
            extract_coordinates(coord, poly_lines)
    else:
        assert len(coordinates) == 2, coordinates
        lg, lat = coordinates
        if is_visible(lg, lat):
            poly_lines.append(lg)
            poly_lines.append(lat)


def extract_lines(input_file, output_file):
    with open(input_file) as f:
        data = json.load(f)

    poly_lines = []
    for feature in data['features']:
        extract_coordinates(feature['geometry']['coordinates'], poly_lines)
        # for coordinate in feature['geometry']['coordinates'][0]:
        #     assert len(coordinate) == 2, coordinate
        #     lg, lat = coordinate
        #     if is_visible(lg, lat):
        #         poly_lines.append(lg)
        #         poly_lines.append(lat)

    with open(output_file, 'w') as f:
        for coord_part in poly_lines:
            f.write(str(coord_part) + '\n')


if __name__ == '__main__':
    extract_lines('WB_countries_Admin0_lowres.geojson', 'europe.txt')
