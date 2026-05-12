-- Table: recipe_request
CREATE TABLE recipe_request (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                content TEXT,
                                type VARCHAR(20) CHECK (type IN ('text', 'audio', 'image')),
                                status VARCHAR(20) DEFAULT 'processing' CHECK (status IN ('processing', 'failed', 'completed')),
                                fail_reason TEXT,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE
);

-- Table: recipe
CREATE TABLE recipe (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        request_id BIGINT REFERENCES recipe_request(id),
                        title VARCHAR(255) NOT NULL,
                        description TEXT,
                        duration INTEGER, -- stored in minutes
                        request_type VARCHAR(20) CHECK (request_type IN ('text', 'audio', 'image')),
                        steps TEXT[], -- PostgreSQL Array type
                        instructions TEXT[],
                        ingredients TEXT[],
                        rich_text_content TEXT, -- For detailed formatting
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE
);

-- Indices for performance
CREATE INDEX idx_recipe_request_user ON recipe_request(user_id);
CREATE INDEX idx_recipe_user ON recipe(user_id);