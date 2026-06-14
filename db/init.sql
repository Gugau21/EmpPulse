-- ─────────────────────────────────────────────────────────────────────────────
-- Enum types
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TYPE user_theme    AS ENUM ('light', 'dark', 'system');
CREATE TYPE user_language AS ENUM ('en', 'ukr');
CREATE TYPE leave_type    AS ENUM ('vacation', 'sick', 'personal');
CREATE TYPE leave_status  AS ENUM ('pending', 'approved', 'rejected', 'cancelled');


-- ─────────────────────────────────────────────────────────────────────────────
-- Default hours schedule set
-- A grouping entity: each row represents one named weekly schedule.
-- Rows in schedule_block reference this table via set_id.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE week_schedule (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Users  (authentication + identity)
-- "user" is a reserved word in PostgreSQL, so we use app_user.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE app_user (
  id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        varchar(100)  NOT NULL,
  surname     varchar(100)  NOT NULL,
  email       text          NOT NULL,
  pass_hash   text          NOT NULL,
  theme       user_theme    NOT NULL DEFAULT 'system',
  language    user_language NOT NULL DEFAULT 'en',
  is_owner    boolean       NOT NULL DEFAULT false,
  active      boolean       NOT NULL DEFAULT false
);

CREATE UNIQUE INDEX uq_app_user_email_active ON app_user (email) WHERE (active = true);
CREATE UNIQUE INDEX uq_app_user_is_owner ON app_user (is_owner) WHERE (is_owner = true);


-- ─────────────────────────────────────────────────────────────────────────────
-- Departments
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE department (
  id                   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name                 text    NOT NULL UNIQUE,
  is_default           boolean NOT NULL DEFAULT false,
  week_schedule_id integer REFERENCES week_schedule (id)
                               ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE UNIQUE INDEX uq_department_is_default ON department (is_default) WHERE (is_default = true);


-- ─────────────────────────────────────────────────────────────────────────────
-- Employees
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE employee (
  id                   integer NOT NULL PRIMARY KEY
                               REFERENCES app_user (id)
                               ON DELETE CASCADE ON UPDATE CASCADE,
  department_id        integer REFERENCES department (id)
                               ON DELETE SET NULL ON UPDATE CASCADE,
  week_schedule_id integer REFERENCES week_schedule (id)
                               ON DELETE SET NULL ON UPDATE CASCADE,
  vacation_balance     integer NOT NULL DEFAULT 0,
  active               boolean GENERATED ALWAYS AS (department_id IS NOT NULL) STORED NOT NULL
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Admins
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE admin (
  id integer NOT NULL PRIMARY KEY
                  REFERENCES app_user (id)
                  ON DELETE CASCADE ON UPDATE CASCADE,
  active  boolean NOT NULL DEFAULT false
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Admin ↔ Department assignments  (composite PK)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE admin_department (
  admin_id      integer NOT NULL REFERENCES admin (id)
                                 ON DELETE CASCADE ON UPDATE CASCADE,
  department_id integer NOT NULL REFERENCES department (id)
                                 ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (admin_id, department_id)
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Leaves (absence requests)
-- modification_id is a self-reference: points to the leave record this one
-- was created to modify/supersede (edit history chain).
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE leave (
  id                integer      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  employee_id       integer      NOT NULL REFERENCES employee (id)
                                          ON DELETE RESTRICT ON UPDATE CASCADE,
  type              leave_type   NOT NULL,
  start_date        date         NOT NULL,
  end_date          date         NOT NULL,
  paid              boolean      NOT NULL,
  status            leave_status NOT NULL DEFAULT 'pending',
  description       text,
  admin_reviewer_id integer      REFERENCES admin (id)
                                 ON DELETE RESTRICT ON UPDATE CASCADE,
  admin_comment     text,
  modification_id   integer      REFERENCES leave (id)
                                 ON DELETE SET NULL,
  created_at        timestamptz  NOT NULL DEFAULT now(),
  updated_at        timestamptz  NOT NULL DEFAULT now(),

  CONSTRAINT leave_dates_valid CHECK (end_date >= start_date),
  CONSTRAINT leave_description_required CHECK (type <> 'personal' OR description IS NOT NULL)
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Logged hours  (time entries per employee)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE logged_hours (
  id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  employee_id integer NOT NULL REFERENCES employee (id)
                               ON DELETE RESTRICT ON UPDATE CASCADE,
  admin_id    integer NOT NULL REFERENCES admin (id)
                               ON DELETE RESTRICT ON UPDATE CASCADE,
  date        date    NOT NULL,
  start_time  time    NOT NULL,
  end_time    time    NOT NULL,
  paid        boolean NOT NULL,

  CONSTRAINT logged_hours_times_valid CHECK (end_time > start_time)
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Default hours  (weekly schedule entries, grouped by set_id)
-- day_of_week: 0 = Sunday … 6 = Saturday
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE schedule_block (
  id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  set_id      integer  NOT NULL REFERENCES week_schedule (id)
                                ON DELETE CASCADE ON UPDATE CASCADE,
  day_of_week smallint NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
  start_time  time     NOT NULL,
  end_time    time     NOT NULL,
  paid        boolean  NOT NULL,

  CONSTRAINT schedule_block_times_valid CHECK (end_time > start_time),
  UNIQUE (set_id, day_of_week)
);


-- ─────────────────────────────────────────────────────────────────────────────
-- Indexes
-- ─────────────────────────────────────────────────────────────────────────────

CREATE INDEX ON employee      (department_id);
CREATE INDEX ON leave         (employee_id);
CREATE INDEX ON leave         (start_date);
CREATE INDEX ON leave         (updated_at);
CREATE INDEX ON logged_hours  (employee_id);
CREATE INDEX ON logged_hours  (date);
CREATE INDEX ON schedule_block (set_id);


-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE bonus_vacation_days(
	id integer PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  days integer NOT NULL,
  employee_id integer NOT NULL
                      REFERENCES employee (id)
                      ON DELETE CASCADE ON UPDATE CASCADE,
  year_ integer NOT NULL
);

CREATE UNIQUE INDEX ON bonus_vacation_days (employee_id, year_);


-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION sync_user_active()
RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
  v_user_id integer;
BEGIN
  v_user_id := CASE WHEN TG_OP = 'DELETE' THEN OLD.id ELSE NEW.id END;

  UPDATE app_user
  SET active = (
    is_owner
    OR EXISTS (SELECT 1 FROM employee WHERE id = v_user_id AND active = true)
    OR EXISTS (SELECT 1 FROM admin    WHERE id = v_user_id AND active = true)
  )
  WHERE id = v_user_id;

  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_employee_sync_user_active
AFTER INSERT OR UPDATE OF active OR DELETE ON employee
FOR EACH ROW EXECUTE FUNCTION sync_user_active();

CREATE TRIGGER trg_admin_sync_user_active
AFTER INSERT OR UPDATE OF active OR DELETE ON admin
FOR EACH ROW EXECUTE FUNCTION sync_user_active();


-- ─────────────────────────────────────────────────────────────────────────────
-- Owner is always active: app_user is inserted directly (no employee/admin row),
-- so a BEFORE trigger sets active at insert time. sync_user_active() additionally
-- keeps is_owner in its derivation so later employee/admin changes can't unset it.
-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION sync_owner_active()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.is_owner THEN
    NEW.active := true;
  END IF;
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_app_user_owner_active
BEFORE INSERT OR UPDATE OF is_owner ON app_user
FOR EACH ROW EXECUTE FUNCTION sync_owner_active();


-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION sync_admin_active()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    UPDATE admin SET active = true
    WHERE id = NEW.admin_id;

  ELSIF TG_OP = 'DELETE' THEN
    UPDATE admin SET active = EXISTS (
      SELECT 1 FROM admin_department WHERE admin_id = OLD.admin_id
    )
    WHERE id = OLD.admin_id;
  END IF;
  RETURN NULL;
END;
$$;

CREATE TRIGGER trg_admin_active
AFTER INSERT OR DELETE ON admin_department
FOR EACH ROW EXECUTE FUNCTION sync_admin_active();


-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_leave_updated_at
BEFORE UPDATE ON leave
FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);
CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BYTEA NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);