package com.resume.mapper;

import com.resume.entity.Template;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TemplateMapper {

    @Select("SELECT * FROM templates WHERE is_public = #{isPublic} ORDER BY is_official DESC, usage_count DESC")
    List<Template> findByIsPublicOrderByIsOfficialDescUsageCountDesc(@Param("isPublic") Boolean isPublic);

    @Select("SELECT * FROM templates WHERE category = #{category} AND is_public = #{isPublic} ORDER BY usage_count DESC")
    List<Template> findByCategoryAndIsPublicOrderByUsageCountDesc(@Param("category") String category, @Param("isPublic") Boolean isPublic);

    @Select("SELECT * FROM templates WHERE id = #{id}")
    Template findById(@Param("id") Long id);

    @Insert("INSERT INTO templates (name, category, description, preview_url, content, layout, theme_config, section_config, section_order, is_official, is_public, created_by, usage_count, created_at, updated_at) " +
            "VALUES (#{name}, #{category}, #{description}, #{previewUrl}, #{content}, #{layout}, #{themeConfig}, #{sectionConfig}, #{sectionOrder}, #{isOfficial}, #{isPublic}, #{createdBy}, #{usageCount}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Template template);

    @Update("UPDATE templates SET name = #{name}, category = #{category}, description = #{description}, preview_url = #{previewUrl}, " +
            "content = #{content}, layout = #{layout}, theme_config = #{themeConfig}, section_config = #{sectionConfig}, section_order = #{sectionOrder}, " +
            "is_official = #{isOfficial}, is_public = #{isPublic}, updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Template template);

    @Delete("DELETE FROM templates WHERE id = #{id}")
    void delete(@Param("id") Long id);

    @Update("UPDATE templates SET usage_count = usage_count + 1 WHERE id = #{id}")
    void incrementUsage(@Param("id") Long id);
}
