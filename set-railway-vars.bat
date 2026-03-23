@echo off
railway variables --service resume-builder set "SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}"
railway variables --service resume-builder set "SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}"
railway variables --service resume-builder set "SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}"
echo Done.
