-- Drop columns that are no longer needed in the new schema
ALTER TABLE recipe
DROP COLUMN IF EXISTS duration,
    DROP COLUMN IF EXISTS rich_text_content,
    DROP COLUMN IF EXISTS ingredients;

-- Drop steps separately because we need to change its type from TEXT[] to JSONB
-- Cannot ALTER column type directly in PostgreSQL, so we drop and re-add
ALTER TABLE recipe
DROP COLUMN IF EXISTS steps;

-- Add all new columns
ALTER TABLE recipe
    ADD COLUMN prep_time_mins    INTEGER,                                                    -- preparation time in minutes
    ADD COLUMN cook_time_mins    INTEGER,                                                    -- cooking time in minutes
    ADD COLUMN cuisine           VARCHAR(100),                                               -- e.g. Pakistani, Italian
    ADD COLUMN difficulty        VARCHAR(50) CHECK (difficulty IN ('Easy', 'Medium', 'Hard')), -- recipe difficulty level
    ADD COLUMN servings          INTEGER,                                                    -- number of servings
    ADD COLUMN tags              TEXT[],                                                     -- array of tags for filtering
    ADD COLUMN ingredients_list  TEXT[],                                                     -- ingredient names only, used for quick search
    ADD COLUMN ingredients       JSONB,                                                      -- full ingredient objects (name, quantity, unit, notes)
    ADD COLUMN steps             JSONB,                                                      -- full step objects (stepNumber, instruction, tip, durationMinutes)
    ADD COLUMN nutrition_info    JSONB;                                                      -- nutrition data (calories, protein, carbs, fat, fiber)

-- GIN index on tags array for fast tag-based filtering
CREATE INDEX idx_recipe_tags             ON recipe USING GIN(tags);

-- GIN index on ingredients_list array for fast ingredient-based search
CREATE INDEX idx_recipe_ingredients_list ON recipe USING GIN(ingredients_list);

-- B-tree indexes on commonly filtered relational columns
CREATE INDEX idx_recipe_cuisine          ON recipe(cuisine);
CREATE INDEX idx_recipe_difficulty       ON recipe(difficulty);