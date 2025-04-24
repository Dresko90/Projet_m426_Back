mysql -h db -u root -pepai123 -e 'DROP DATABASE IF EXISTS  messaging_db;'
mysql -h db -u root -pepai123 < messaging-db.sql
mysql -h db -u root -pepai123 < messaging-test-data.sql