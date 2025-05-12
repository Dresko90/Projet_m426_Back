# mvn flyway:clean && mvn flyway:migrate

mysql -h db -u root -pepai123 -e 'DROP DATABASE IF EXISTS  messaging_db;'
mysql -h db -u root -pepai123 -e 'CREATE DATABASE messaging_db DEFAULT CHARSET UTF8;'

#mysql -h db -u root -pepai123 < data/1_create-db.sql
#mysql -h db -u root -pepai123 < data/2_instert-demo-data.sql