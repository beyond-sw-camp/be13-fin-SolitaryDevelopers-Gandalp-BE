package com.gandalp.gandalp.member.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNurse is a Querydsl query type for Nurse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNurse extends EntityPathBase<Nurse> {

    private static final long serialVersionUID = -49550777L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNurse nurse = new QNurse("nurse");

    public final com.gandalp.gandalp.common.entity.QBaseEntity _super = new com.gandalp.gandalp.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final com.gandalp.gandalp.hospital.domain.entity.QDepartment department;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath no = createString("no");

    public final StringPath password = createString("password");

    public final EnumPath<Type> type = createEnum("type", Type.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public final EnumPath<Status> workingStatus = createEnum("workingStatus", Status.class);

    public QNurse(String variable) {
        this(Nurse.class, forVariable(variable), INITS);
    }

    public QNurse(Path<? extends Nurse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNurse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNurse(PathMetadata metadata, PathInits inits) {
        this(Nurse.class, metadata, inits);
    }

    public QNurse(Class<? extends Nurse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.department = inits.isInitialized("department") ? new com.gandalp.gandalp.hospital.domain.entity.QDepartment(forProperty("department"), inits.get("department")) : null;
    }

}

