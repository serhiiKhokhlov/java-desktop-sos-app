$schema=@'
create schema if not exists sos_db;
use sos_db;
drop table if exists vote;
drop table if exists survey_option;
drop table if exists participation;
drop table if exists invitation;
drop table if exists survey;
drop table if exists user;

create table `user` (id int auto_increment primary key,
                   username varchar(30) not null unique,
                   email varchar(40) not null unique,
                   password varchar(30) not null);

create table survey (id int auto_increment primary key,
                     created_by int not null,
                     foreign key (created_by)
                                   references `user`(id) on delete cascade,
                     label varchar(30) not null,
                     description varchar(255) not null,
                     created_at datetime not null,
                     joinkey varchar(40) not null unique,
                     open tinyint(1) not null);

create table invitation (user_id int not null,
                         foreign key (user_id)
                                references `user`(id) on delete cascade,
                         survey_id int not null,
                         foreign key (survey_id)
                                references survey(id) on delete cascade,
                         constraint invitation_user_survey_ids_pk primary key (user_id, survey_id)
                         );

create table participation (user_id int not null,
                            foreign key (user_id)
                                   references `user`(id) on delete cascade,
                            survey_id int not null,
                            foreign key (survey_id)
                                   references survey(id) on delete cascade,
                            constraint participation_user_survey_ids_pk primary key (user_id, survey_id)
                           );

create table survey_option (id int auto_increment primary key,
                            timeOption datetime not null,
                            survey_id int not null,
                            foreign key (survey_id)
                                references survey(id) on delete cascade
                           );

create table vote (user_id int not null,
                    foreign key (user_id) references `user`(id) on delete cascade,
                    survey_option_id int not null,
                    foreign key (survey_option_id) references survey_option(id) on delete cascade,
                    constraint vote_user_option_ids_pk primary key (user_id, survey_option_id),
                    is_preferred tinyint(1)
                   );

                   -- ---------------------------------
-- MOCK DATA based on FakeRepository
-- ---------------------------------

-- Users (IDs 1, 2, 3)
INSERT INTO user (id, username, password, email) VALUES (1, 'admin', '1234', 'admin@example.com');
INSERT INTO user (id, username, password, email) VALUES (2, 'user', 'user123', 'user@example.com');
INSERT INTO user (id, username, password, email) VALUES (3, 'john', 'john123', 'john@example.com');

-- Surveys (IDs 1, 2)
-- Survey 1: Team Meeting (created by admin)
INSERT INTO survey (id, created_by, label, description, created_at, joinkey, open)
VALUES (1, 1, 'Team Meeting', 'Weekly sync', NOW(), 'team-meeting-key', 1);
-- Survey 2: Project Review (created by user)
INSERT INTO survey (id, created_by, label, description, created_at, joinkey, open)
VALUES (2, 2, 'Project Review', 'Q2 Planning', NOW(), 'project-review-key', 1);

-- Invitations for Survey 1
INSERT INTO invitation (user_id, survey_id) VALUES (3, 1); -- john is invited
INSERT INTO invitation (user_id, survey_id) VALUES (2, 1); -- user is invited

-- Participations
-- Survey 1: Admin is creator, user joins to vote
INSERT INTO participation (user_id, survey_id) VALUES (1, 1);
INSERT INTO participation (user_id, survey_id) VALUES (2, 1);
-- Survey 2: User is creator
INSERT INTO participation (user_id, survey_id) VALUES (2, 2);

-- Survey Options for Survey 1 (Team Meeting)
INSERT INTO survey_option (id, survey_id, timeOption) VALUES (1, 1, DATE_ADD(DATE(NOW()), INTERVAL '1 14' DAY_HOUR));
INSERT INTO survey_option (id, survey_id, timeOption) VALUES (2, 1, DATE_ADD(DATE(NOW()), INTERVAL '2 10' DAY_HOUR));

-- Survey Options for Survey 2 (Project Review)
INSERT INTO survey_option (id, survey_id, timeOption) VALUES (3, 2, DATE_ADD(DATE(NOW()), INTERVAL '3 15' DAY_HOUR));
INSERT INTO survey_option (id, survey_id, timeOption) VALUES (4, 2, DATE_ADD(DATE(NOW()), INTERVAL '2 15' DAY_HOUR));

-- Votes
-- Votes for Survey 1, Option 1
INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (1, 1, 0); -- admin votes
-- Votes for Survey 1, Option 2
INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (1, 2, 1); -- admin votes and prefers
INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (2, 2, 0); -- user votes
-- Votes for Survey 2, Option 3
INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (2, 3, 0); -- user votes
-- Votes for Survey 2, Option 4
INSERT INTO vote (user_id, survey_option_id, is_preferred) VALUES (2, 4, 0); -- user votes
'@

echo $schema | docker exec -i mysql mysql
