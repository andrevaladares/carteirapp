package br.com.carteira.repository

import groovy.sql.Sql

class DBUtils {
    static dbDriver = 'com.mysql.cj.jdbc.Driver'
    static dbURL = 'jdbc:mysql://localhost:3306/carteirapptests?useTimezone=true&serverTimezone=UTC'
    static dbUser = 'carteirapptests'
    static dbPass = '43878784'

    static Sql getSqlConnection() {
        Sql.newInstance(dbURL, dbUser, dbPass, dbDriver)
    }
}
