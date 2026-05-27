-- V5: drop unused extensibility columns (Issue 31 / 54).
--
-- The 15 columns dropped here are unused extensibility scratch fields:
--   star_obj:     misc_text1..5  (5 varchar)
--                 misc_num1..5   (5 double)
--   solar_system: custom_data1..5 (5 varchar)
--
-- Audited callers in src/main:
--   - AstroCSVStar     (CSV import row mapper) wrote them on import
--   - StarEditFormBinder + StarEditDialog "User Special Info" tab read/wrote them
--   - Both removed in the same commit as this migration
--
-- All other entity surface (DataSetDescriptor.custom_data_defs_str + _values_str,
-- DataSetDescriptor.theme_str, etc.) is intentionally NOT touched — those
-- carry real dataset-level extensibility used by the import/export pipeline.
--
-- Reversibility: a downgrade would need to ALTER TABLE ADD COLUMN back; the
-- column values were uniformly zero/empty/null per the audit, so any later
-- re-creation can use default zero/null defaults.

ALTER TABLE star_obj DROP COLUMN misc_text1;
ALTER TABLE star_obj DROP COLUMN misc_text2;
ALTER TABLE star_obj DROP COLUMN misc_text3;
ALTER TABLE star_obj DROP COLUMN misc_text4;
ALTER TABLE star_obj DROP COLUMN misc_text5;

ALTER TABLE star_obj DROP COLUMN misc_num1;
ALTER TABLE star_obj DROP COLUMN misc_num2;
ALTER TABLE star_obj DROP COLUMN misc_num3;
ALTER TABLE star_obj DROP COLUMN misc_num4;
ALTER TABLE star_obj DROP COLUMN misc_num5;

ALTER TABLE solar_system DROP COLUMN custom_data1;
ALTER TABLE solar_system DROP COLUMN custom_data2;
ALTER TABLE solar_system DROP COLUMN custom_data3;
ALTER TABLE solar_system DROP COLUMN custom_data4;
ALTER TABLE solar_system DROP COLUMN custom_data5;
