package com.resume.mapper;

import com.resume.entity.Resume;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {

    @Select("SELECT * FROM resumes WHERE user_id = #{userId} ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Resume> findByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM resumes WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM resumes WHERE user_id = #{userId} AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Resume> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM resumes WHERE user_id = #{userId} AND (#{status} IS NULL OR status = #{status})")
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM resumes WHERE id = #{id}")
    Resume findById(@Param("id") Long id);

    @Insert("INSERT INTO resumes (user_id, title, template_id, content, section_order, status, is_primary, created_at, updated_at) " +
            "VALUES (#{userId}, #{title}, #{templateId}, #{content}::json, #{sectionOrder}::json, #{status}, #{isPrimary}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resume resume);

    @Update("UPDATE resumes SET title = #{title}, template_id = #{templateId}, content = #{content}::json, section_order = #{sectionOrder}::json, " +
            "status = #{status}, is_primary = #{isPrimary}, updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Resume resume);

    @Delete("DELETE FROM resumes WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT * FROM resumes WHERE user_id = #{userId} AND is_primary = #{isPrimary}")
    List<Resume> findAllByUserIdAndIsPrimary(@Param("userId") Long userId, @Param("isPrimary") Boolean isPrimary);

    @Update("UPDATE resumes SET is_primary = false WHERE user_id = #{userId} AND is_primary = true AND id != #{excludeId}")
    void clearPrimaryForUser(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}
