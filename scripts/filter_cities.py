import csv

min_population = 500_000

def read_csv(file_path):
    def safe_str_to_int(s):
        try:
            return int(s)
        except ValueError:
            try:
                return int(float(s))
            except ValueError:
                return 0

    with open(file_path, mode='r', newline='', encoding='utf-8') as csvfile:
        csvreader = csv.reader(csvfile)
        header = next(csvreader)
        rows = [row for row in csvreader if safe_str_to_int(row[header.index('population')]) >= min_population]
    return header, rows

if __name__ == '__main__':
    file_path = '../assets//worldcities.csv'
    header, rows = read_csv(file_path)
    print(",".join(header))
    # print(len(rows), "rows")
    for row in rows:
        print(",".join(f'"{cell}"' for cell in row))

