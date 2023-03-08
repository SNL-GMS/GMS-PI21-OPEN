from sqlalchemy import create_engine
from sqlalchemy.sql import text
import sqlalchemy.engine.url as url
import cx_Oracle
import os


def test_dir_exists(file_dir):
    if os.path.isdir(file_dir[0]):
        print("Directory " + file_dir[0] + " exists.")
        return True
    else:
        print("An error occurred for directory: " + file_dir[0])
        print("Make sure the directory is formatted for Linux.")
        return False


def test_directory(file_dir, result_files):
    files = os.listdir(file_dir[0])
    count = 0

    for file in result_files:
        if file[0] not in files:
            print("DID NOT FIND: " + file[0])
            count = count + 1

    if count == 0:
        print("All files listed found for directory: " + file_dir[0])
    else:
        print("Did not find " + str(count) + " files in directory: " + file_dir[0])


# This script checks where the wfdiscs are located and checks to see if the directory exists.
dbinfo = url.URL('oracle+cx_oracle', username='/@gms_al1_ro')
db_engine = create_engine(dbinfo, max_identifier_length=128)
connection = db_engine.connect()

query_min_date = text("SELECT min(jdate) FROM ARRIVAL")
query_max_date = text("SELECT max(jdate) FROM ARRIVAL")

date_begin_result = connection.execute(query_min_date)
date_end_result = connection.execute(query_max_date)

for st in date_begin_result:
    date_begin = st[0]

for st1 in date_end_result:
    date_end = st1[0]

query_wfdisc_dir = text("SELECT distinct(DIR) FROM WFDISC WHERE JDATE >= :x AND JDATE <= :y")
result_wfdisc_dir = connection.execute(query_wfdisc_dir, x=date_begin, y=date_end)

print("Expected directories for the location of the wfdiscs in the time frame of " + str(date_begin) + " and " +
      str(date_end) + ":")

for directory in result_wfdisc_dir:
    print("WFDISC DIRECTORY: " + directory[0])

    query_wfdisc_file = text("SELECT distinct(dfile) FROM WFDISC WHERE dir = :x")
    result_wfdisc_file = connection.execute(query_wfdisc_file, x=directory[0])
    if test_dir_exists(directory):
        test_directory(directory, result_wfdisc_file)

# Response files
query_instrument_dir = text("SELECT distinct(DIR) FROM INSTRUMENT")
result_instrument_dir = connection.execute(query_instrument_dir)
print("Expected directories for the location of the response files:")
for directory in result_instrument_dir:
    print("RESPONSE directory: " + directory[0])

    query_instrument_file = text("SELECT distinct(dfile) FROM INSTRUMENT WHERE dir = :x")
    result_instrument_file = connection.execute(query_instrument_file, x=directory[0])
    if test_dir_exists(directory):
        test_directory(directory, result_instrument_file)

connection.close()
