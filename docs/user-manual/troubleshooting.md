# Troubleshooting

## App does not start

- Confirm you are using a supported Java version.
- If running a packaged build, ensure the installer completed successfully.

## Missing data or icons

- Verify `files/programdata/` exists and contains the CSV and PNG files.
- Check file permissions for the `files/` directory.

## Slow startup

- Large databases can slow startup. See internal notes for database management and rebuild options.
- If the database is corrupt, delete it and re-import datasets.

## Reporting Problems

Use **Help > Report a Problem** to send a report with logs and diagnostics.
