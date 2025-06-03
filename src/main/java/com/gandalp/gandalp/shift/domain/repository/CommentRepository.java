package com.gandalp.gandalp.shift.domain.repository;

import com.gandalp.gandalp.shift.domain.entity.Board;
import com.gandalp.gandalp.shift.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT b FROM Board b " +
            "LEFT JOIN FETCH b.comments c " +
            "LEFT JOIN FETCH c.nurse " +
            "WHERE b.id = :boardId")
    Optional<Board> findByIdWithCommentsAndNurse(@Param("boardId") Long boardId);

//    List<Comment> findByBoardId(Long boardId);


}
