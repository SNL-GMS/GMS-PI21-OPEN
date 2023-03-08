from sqlalchemy import create_engine
from sqlalchemy.sql import text
import sqlalchemy.engine.url as url
import cx_Oracle

# This script checks the station groups that are expected and checks them against the database's network table.
dbinfo = url.URL('oracle+cx_oracle', username='/@gms_al1_ro')
db_engine = create_engine(dbinfo, max_identifier_length=128)
connection = db_engine.connect()

station_groups = ["ALL_1", "ALL_2", "A_TO_H", "I_TO_Z", "EurAsia", "OthCont", "IMS_Sta", "CD1.1", "CD1.0",
                  "MiniSD", "GSE", "Primary", "Second", "AuxFast", "AuxDel", "SEISMIC", "INFRA", "HYDRO"]

query_station_group = text("SELECT network_name FROM network WHERE net = :x")

output1 = "Script succeeded? "
output2 = "Failed groups:\n"
success = True
for group in station_groups:
    result = connection.execute(query_station_group, x=group)
    row = result.fetchone()
    if row is None:
        success = False
        output2 = output2 + group

print(output1 + str(success))
if not success:
    print(output2)

connection.close()

