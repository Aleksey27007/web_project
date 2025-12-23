package com.app.totalizator.service.factory;

import com.app.totalizator.connection.ConnectionPool;
import com.app.totalizator.dao.UserDao;
import com.app.totalizator.dao.impl.UserDaoImpl;

public class DaoFactory {
    private static final DaoFactory instance = new DaoFactory();
    private final ConnectionPool connectionPool;


    private DaoFactory() {
        this.connectionPool = ConnectionPool.getInstance();
    }

    public static DaoFactory getInstance() {
        return instance;
    }

    public UserDao getUserDao() {
        return new UserDaoImpl(connectionPool);
    }

//    public CompetitionDao getCompetitionDao() {
//        return new CompetitionDaoImpl(connectionPool);
//    }
//
//    public BetDao getBetDao() {
//        return new BetDaoImpl(connectionPool, getUserDao(), getCompetitionDao());
//    }
//
//    public Dao<BetType, Integer> getBetTypeDao() {
//        return new BetTypeDaoImpl(connectionPool);
//    }
}
