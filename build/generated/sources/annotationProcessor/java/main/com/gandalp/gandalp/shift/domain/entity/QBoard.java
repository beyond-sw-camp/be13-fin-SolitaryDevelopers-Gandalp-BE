package com.gandalp.gandalp.shift.domain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = 83784986L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoard board = new QBoard("board");

    public final com.gandalp.gandalp.common.entity.QBaseEntity _super = new com.gandalp.gandalp.common.entity.QBaseEntity(this);

    public final EnumPath<BoardStatus> boardStatus = createEnum("boardStatus", BoardStatus.class);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final com.gandalp.gandalp.hospital.domain.entity.QDepartment department;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.gandalp.gandalp.member.domain.entity.QMember member;

    public final com.gandalp.gandalp.member.domain.entity.QNurse nurse;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath updatedBy = _super.updatedBy;

    public QBoard(String variable) {
        this(Board.class, forVariable(variable), INITS);
    }

    public QBoard(Path<? extends Board> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoard(PathMetadata metadata, PathInits inits) {
        this(Board.class, metadata, inits);
    }

    public QBoard(Class<? extends Board> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.department = inits.isInitialized("department") ? new com.gandalp.gandalp.hospital.domain.entity.QDepartment(forProperty("department"), inits.get("department")) : null;
        this.member = inits.isInitialized("member") ? new com.gandalp.gandalp.member.domain.entity.QMember(forProperty("member"), inits.get("member")) : null;
        this.nurse = inits.isInitialized("nurse") ? new com.gandalp.gandalp.member.domain.entity.QNurse(forProperty("nurse"), inits.get("nurse")) : null;
    }

}

