# Business Logic

## Definitions

**User** - an entity linked to one person, sufficient for person authentication.
Contains:
* email
* password
* name
* surname
* preferences

**Employee** - an entity linked to **User**, which allows their working data to be tracked. **Employee** may be in the one **Department**.
Contains:
* yearly vacation balance
* active

Linked:
* (ZERO.ONE-ONE) **User**
* (ZERO.MANY-ZERO.ONE) **Department**
* (ONE-ZERO.ONE) **Default Week hours**

**Bonus vacation days** - an entity representing Bonus vacation days.
Contains:
* year
* number of days

Linked:
* (ONE-ZERO.MANY) **Employee**

**Admin** - an entity linked to **User**, which allows them to oversee **Employees**. **Admin** may oversee many **Departments**.
Contains:
* active

Linked:
* (ZERO.ONE-ONE) **User**
* (ZERO.MANY-ZERO.MANY) **Department**

**Owner** - an entity linked to **User**, which allows them to manage the whole system. There exists only one Owner.

**Department** - an entity for grouping **Employees**.
Contains:
* name

Linked:
* (ZERO.MANY-ZERO.MANY) **Admin**
* (ONE-ZERO.ONE) **Default Week hours**

**Leave request** - an entity representing a planned absence of **Employee**.
Contains:
* leave type
* paid type (paid/unpaid)
* reason
* status
* comment
* start date
* end date

Leave types include: Vacation, Sick, and Personal.
Statuses include Pending, Accepted, Rejected, Cancelled.

Linked:
* (ZERO.MANY-ONE) **Employee**
* (ZERO.ONE-ZERO.ONE) modification **Leave request**
* (ZERO.MANY-ZERO.ONE) **Admin**

**Logged hours** - an entity representing a period of time of **Employee** logged in the system.
Contains:
* date
* start time
* end time

Linked:
* (ZERO.ONE-ONE) **Employee**
* (ZERO.ONE-ONE) **Admin**
 
**Default Week hours** - an entity for grouping **Logged hours** in week.
Linked:
* (ZERO.ONE-MANY) **Logged hours**

**Logged hours Overlap** – A situation which occures when two **Logged hours** for the same **Employee** on the same date share any portion of time. This includes cases where one period is contained within another, or where the start/end times intersect.

**Logged hours Adjacency** – A situation which occures when the end time of one **Logged hours** exactly matches the start time of another for the same **Employee** on the same date.

**Session** - an entity that links **User** and device.
Contains:
* token

Linked:
* **User**

**Default Department** - a **Department** for **Users** who are not assigned to other **Departments**.

**System** - a **Admin** that represents automatic actions of system.

*field name*

**Employee** is in **Department**.
**Department** contains **Employee**.
**Admin** oversees **Department**.
**Department** is overseen by **Admin**.

**User** is active if its field *active* is true.
**User** is inactive if its field *active* is false.

## Main

### Department management

#### Department Existence

1. **Department** must have *name* unique within **Departments**.

#### Department View

1. **Department** is visible only to **Admins** who oversee it.

#### Department Creation

1. **Owner** can create **Department** with name.

> Owner can then edit **Admins**, **Emploees** and **Default Week hours** in one action

#### Department Modification

1. Any changes must oblige rules of existence.
2. **Admin** can change **Default Week hours**.
3. **Owner** can additionally change *name* of **Department**.
4. **Employee** can not leave their **Department**.
5. **Admin** can not attach or detach themselves or other **Admins** from a **Department**.
6. **Admin** can move **Employees** across **Departments** they oversee.
7. **Owner** can attach **Admin** from **Department**.
8. **Owner** can detach **Admin** from **Department** if **Admin** oversees more than 2.

#### Department Deletion

1. **Owner** can delete a **Department** if no Admin oversees it and **Department** does not have ane **Employees**.
2. **Default Department** cannot be deleted.

### User management

#### User Existence

1. If a **User** has at least 1 link to active **Admin** or active **Employee** then **User** is active, otherwise they are inactive.
1. Active **User** must have all its fields filled.
2. Active **User** must have unique *email* within active **Users** and valid by email format convention.
3. **User** must have a link to either to **Admin** or **Employee**.

:::info
**User** can function as both an **Employee** and an **Admin** entity simultaneously. A **User** may be considered an **Employee** in one Department while serving as the **Admin** of another.
:::

5. **User** can be **Owner** only if they are an **Admin** for all **Departments**.
>But an **Admin** for all **Departments** is not automtically **Owner**.
6. **Employee** must have *yearly vacation balance* filled.
7. **Employee** must have a link to a **User**.
8. **Admin** must have a link to a **User**

:::warning
Due to codependency, **User** and (**Employee** or **Admin**) must be created in the same action.
:::

#### User View

1. Only active **Users** can be viewed.
2. **User** can view their own data.
3. **Admin** can view data of **Users** who are **Employees** in **Departments** they oversee.
4. **Owner** can view data of all **Admins**.

>Therefore all **Users**.

#### User Creation

1. **User** creation must not break **User** existence rules.
2. **Admin** can create **User** linked to **Employee**.
3. **Owner** can create **User** linked to **Admin**.
4. **User** creation requires name, surname, email, password.
5. System sends account credentials to the registered email.
6. **Employee** creation requies yearly vacation balance.
7. **Employee** creation requies a link to a **Department** overseen by **Admin** who is creating **Employee**.
8. **Admin** creation requies a link to a **Department**, default is **Default Department**.
9. Created **Employee** is active.
10. Created **Admin** is active.
>Owner can create **User** linked to **Admin** and **Employee**.

#### User Modification

1. **User** Modification must not break **User** existence rules.
2. **User** Modification is possible if **User** is visible.
3. **User** can modify their password.
4. **Admin** can modify **Default Week hours** of **Employee** in **Departments** they oversee.
5. **Admin** can modify which **Department** an **Employee** is in. See rules for **Department** Modification.
6. **Admin** can modify BonusVacationDays of an **Employee**
7. **Owner** can modify all parameters of **User** (except preferences), **Employee** and **Admin**.
8. If **Owner** modifies **User** password they will receive email with credentials (Same as in User Creation).
9. **Owner** can attach **User** to **Employee** or **Admin**.
10. If **Owner** changed **Employee** to inactive
    * they are deatched from **Department**;
    * **Employee**'s pending **Leave requests** are deleted.
11. If **Owner** changed **Admin** to inactive, they are detached from all their **Departments**.

#### User Deletion

1. **Users**, **Employees** and **Admins** are never deleted.

### Bonus vacation days management

#### Bonus vacation days Existence

1. All fields of  **Bonus vacation days** must be filled.
2. **Bonus vacation days** must have a link to an **Employee**.
3. **Bonus vacation days** must have a unique pair of **Employee** and *year* within **Bonus vacation days**.

#### Bonus vacation days View

1. **Bonus vacation days** is visible to **Admins** who oversee the **Department** of linked **Employee**.

#### Bonus vacation days Creation

1. Creation must not break rules of Existence.
2. Admin can create **Bonus vacation days** with **Employee** they oversee, year and number of days.

#### Bonus vacation days Modification

1. Modification must not break rules of Existence.
2. Modification is posible only with visible **Bonus vacation days**
3. Admin can alter *number of days* of **Bonus vacation days**.

#### Bonus vacation days Modification

1. If *number of days* is 0, **Bonus vacation days** is deleted.

### Authentication and session management

#### Sign in

1. Login requires email and password.
2. Invalid email or password blocks access and returns authentication error.

#### Password change and recovery

1. Changing password when already logged in: requires current password, new password.
3. Incorrect current password returns validation error.
4. Once password is changed **User** receives email notification.
5. Changing password without being logged in: requires email and sends a reset password email.
6. Password reset token expires after 15 minutes.
7. New reset request invalidates previous reset link.

### Leave request

#### Leave request Existence

1. **Leave request** must have filled *leave type*, *paid type* and *status*.
2. **Leave request** must have a link to **Employee**.
3. If *status* is Personal, then *reason* must be fiiled.
4. *Start date* must not be after *end date*.
5. **Leave request** is active if its *status* is Pending or Accepted, otherwise inactive
6. Modification request a **Leave request** that has an incoming link to other **Leave request**.
7. Range of active **Leave request** between *start date* and *end date* must not overlap with simular ranges in active **Leave requests** if they are not linked.

#### Leave Request View

1. **Employee** can only view data of his own **Leave requests**.
2. **Admin** can only view data of a **Leave request** if it was created by an **Employee** of a **Department** which they oversee.

#### Leave Request Creation

1. Active **Employee** can create a **Leave request** with:
    * start date;
    * end date;
    * leave type;
    * paid type (default for Vacation and Sick *leave type* is paid, otherwise unpaid);
    * reason (required for Personal *leave type*).

    Status is set to Pending. 
    If **Employee** is also an **Admin** then the status is set to Accepted.
    **Leave request** is linked to **Employee**.
2. Active **Administrator** can create request for any **Employee** in **Departments** they oversee with:
    * start date;
    * end date;
    * leave type;
    * paid type (default for Vacation and Sick *leave type* is paid, otherwise unpaid);
    * reason (required for Personal *leave type*);
    * linked **Employee**.

    Status is set to Accepted. 
>This includes creating a request on behalf of themselves.
>Admin may add comment in the same action.
8. System sends notification email to **Employee** when a request is created on their behalf.

#### Leave Request Modification

1. Modification must not break rules of Existence.
2. Modification is posible if a **Leave Request** is visible.
3. **Employee** can set *status* of **Leave Request** only from Accepted to Canceled.
4. **Employee** can modify:
    * start date;
    * end date;
    * leave type;
    * paid type;
    * reason.
    of Pending their **Leave Request**
5. **Employee** can create a modification request to their own Accepted Leave requests. This creates a request linked to existing one.
6. A modification request can only be created if at least one field is changed.
7. **Admin** can only edit active **Leave requests** with no modification request if they were created by an **Employee** of a **Department** which they oversee.
8. **Administrator** can edit all fields of a **Leave request**.
9. Pending **Leave request** modified by **Administrator** are Accepted status by default.
10. **Administrator** can approve or reject **Leave requests**, which modifies status of the **Leave request**.
11. If a **Leave Request** modification is accepted it overrides the data of the original **Leave request** and then self deletes.
12. If a **Leave Request** modification is rejected it unlinks from original and becomes a standalone **Leave request**, with rejected status.
14. System sends notification email to **Employee** when their request is modified not by them.
15. If **Leave request** is "Accepted" and unpaid, then all **Logged hours** linked to the **Employee** with date between start and end is deleted.

<h4>
    Leave Request Deletion
</h4>

1. **Employee** can delete own Pending **Leave request**.
2. **Administrator**/**Owner** cannot delete requests.

<h3>
    Working hours logging
</h3>

<h4>
    Logged work hours view
</h4>

1. **Employee** can only view data of his own **Logged work hours**.
2. **Admin** can only view data of **Logged work hours** of **Employees** who are in **Departments** they oversee.

<h4>
    Logged work hours creation
</h4>

1. **Employee** cannot create **Logged hours** created on his behalf.
2. **Admin** can create **Logged hours** for **Employees** in **Departments** they oversee.
3. **Logged hours** requires date, start time and end time.
4. End time must be after start time.
5. Future dates are not allowed in manual logging.
6. If a new **Logged hours** overlaps or is adjacent to an existing **Logged hours** for the same **Employee** on the same date, they are merged into a single **Logged hours** using the earliest start time and latest end time from the adjacent **Logged hours**.

<h4>
    Logged work hours modification
</h4>

1. **Employee** cannot modify **Logged hours** created on his behalf.
2. **Admin** can edit **Logged hours** for **Employees** in **Departments** they oversee.
3. **Logged hours** edit allows changes to date, start time, and end time only.
4. End time must be after start time.
5. Future dates are not allowed in manual logging.
6. After edit, adjacent/overlapping **Logged hours** are merged. See Logged work hours creation.

<h4>
    Logged work hours deletion
</h4>

1. **Employee** cannot delete **Logged hours** created on his behalf.
2. **Admin** can delete **Logged hours** for **Employees** in **Departments** they oversee.

<h3>
    Default Week hours
</h3>

<h4>
    Assignment
</h4>

1. **Default Week hours** can be set for Department and Employee.
2. **Default Week hours** are defined per day of week and can contain multiple time intervals for a day.
3. Each time interval requires start time and end time.
4. End time must be after start time.
5. Each day defaults to N/A (no intervals) until configured.
6. If Employee has no **Default Week hours**, Employee inherits Department **Default Week hours**.
7. If Department has no **Default Week hours**, Employee has no inherited default.

<h4>
    Automatic log generation
</h4>

1. At midnight, system auto-creates **Logged hours** for following day based on default working hours for that day of week, **Admin** for such log is set to **System**.
2. System creates one **Logged hours** per configured interval for that day.
3. If the day is N/A (no intervals), no **Logged hours** are created.
4. System does not auto-create **Logged hours** for **Employee** with approved unpaid leave on that day.
5. System does not auto-create **Logged hours** for **Employee** if they have an unpaid **Leave request** for this date.

<h3>
    Vacation Balance Calculation
</h3>

For a specific year, **Employees** vacation balance is set to their yearly vacation balance plus *number of days* of **Bonus vacation days** for a **Employees** and year.

<h3>
    Data export
</h3>

<h4>
    Export permissions
</h4>

1. **Administrator** can download formatted CSV data.
2. **Administrator** can export data only for **Departments**/**Employees** they oversee.

<h3>
    User preferences
</h3>

<h4>
    Language
</h4>

1. Supported languages are English and Ukrainian.
2. Selected language translates labels, buttons, and system messages.
3. Language preference persists across logouts.
4. Default language is English if preference is not set.

<h4>
    Theme
</h4>

1. Supported themes are Light and Dark.
2. Theme applies across all user screens.
3. Theme preference persists across logouts.
4. Default theme is Light if preference is not set.