mysql -h db -u root -pepai123 -e 'DROP DATABASE IF EXISTS  message_api;'
mysql -h db -u root -pepai123 < message-api_db.sql
mysql -h db -u root -pepai123 < message-api_test-data.sql