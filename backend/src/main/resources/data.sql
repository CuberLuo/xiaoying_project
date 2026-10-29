-- 种子模板：覆盖多个分类与不同素材数量要求，用于演示分类切换
INSERT INTO template (id, title, category, cover_url, description, required_image_count, accepted_mime_types, max_file_size_bytes)
VALUES
    ('demo-postcard', '旅行明信片', '创意图片', '/demo/postcard-cover.jpg',
     '上传图片，模拟生成一张明信片', 2, '["image/jpeg","image/png"]', 5242880),
    ('demo-pet-portrait', '萌宠写真', '宠物', '/demo/pet-cover.jpg',
     '上传一张宠物照片，模拟生成艺术写真', 1, '["image/jpeg","image/png"]', 5242880),
    ('demo-pet-hug', '宠物拥抱', '宠物', '/demo/hug-cover.jpg',
     '上传两张照片，模拟生成宠物与人拥抱的温馨画面', 2, '["image/jpeg","image/png"]', 5242880),
    ('demo-wedding-invite', '婚礼请柬', '婚礼', '/demo/wedding-cover.jpg',
     '上传两张照片，模拟生成动态婚礼请柬', 2, '["image/jpeg","image/png"]', 5242880),
    ('demo-xmas-hug', '圣诞拥抱视频', '节日', '/demo/xmas-cover.jpg',
     '上传两张照片，模拟生成圣诞主题拥抱视频（演示用占位结果）', 2, '["image/jpeg","image/png"]', 5242880);
