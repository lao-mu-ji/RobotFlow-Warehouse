/*
* Author: Zhou Yiqun
*/
CREATE TABLE Products (
                          ProductID TEXT PRIMARY KEY AUTO_INCREMENT,
                          Name TEXT NOT NULL,
                          Category TEXT NOT NULL,
                          GroupID TEXT NOT NULL,
                          LocationX INTEGER,
                          LocationY INTEGER,
                          Weight DOUBLE NOT NULL
);
