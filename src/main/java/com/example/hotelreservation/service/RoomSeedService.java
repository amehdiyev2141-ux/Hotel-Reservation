package com.example.hotelreservation.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class RoomSeedService {

    private final DataSource dataSource;

    public RoomSeedService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void seedRooms() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            ScriptUtils.executeSqlScript(
                    connection,
                    new EncodedResource(new ClassPathResource("seed-sample-rooms.sql"))
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
