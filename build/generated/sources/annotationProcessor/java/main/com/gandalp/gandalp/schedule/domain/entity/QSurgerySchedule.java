package com.gandalp.gandalp.schedule.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSurgerySchedule is a Querydsl query type for SurgerySchedule
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSurgerySchedule extends EntityPathBase<SurgerySchedule> {

    private static final long serialVersionUID = -136767021L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSurgerySchedule surgerySchedule = new QSurgerySchedule("surgerySchedule");

    public final com.gandalp.gandalp.common.entity.QBaseEntity _super = new com.gandalp.gandalp.common.entity.QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.gandalp.gandalp.hospital.domain.entity.QRoom room;

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public QSurgerySchedule(String variable) {
        this(SurgerySchedule.class, forVariable(variable), INITS);
    }

    public QSurgerySchedule(Path<? extends SurgerySchedule> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSurgerySchedule(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSurgerySchedule(PathMetadata metadata, PathInits inits) {
        this(SurgerySchedule.class, metadata, inits);
    }

    public QSurgerySchedule(Class<? extends SurgerySchedule> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.room = inits.isInitialized("room") ? new com.gandalp.gandalp.hospital.domain.entity.QRoom(forProperty("room"), inits.get("room")) : null;
    }

}

