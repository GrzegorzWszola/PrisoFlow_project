--
-- PostgreSQL database dump
--

\restrict VVPooWBFIwBeaRARrYBea5m9luG7lwjWejelSTotLUqeZAmpj1LphH49aVbIjnl

-- Dumped from database version 17.6 (Debian 17.6-2.pgdg13+1)
-- Dumped by pg_dump version 17.6 (Debian 17.6-2.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: case_criminals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.case_criminals (
    id integer NOT NULL,
    case_id integer NOT NULL,
    criminal_id integer NOT NULL,
    role character varying(50),
    assignment_date date DEFAULT CURRENT_DATE,
    notes text,
    CONSTRAINT chk_role CHECK (((role)::text = ANY (ARRAY[('suspect'::character varying)::text, ('accused'::character varying)::text, ('convicted'::character varying)::text, ('witness'::character varying)::text, ('victim'::character varying)::text])))
);


ALTER TABLE public.case_criminals OWNER TO postgres;

--
-- Name: case_criminals_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.case_criminals_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.case_criminals_id_seq OWNER TO postgres;

--
-- Name: case_criminals_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.case_criminals_id_seq OWNED BY public.case_criminals.id;


--
-- Name: criminal_cases; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.criminal_cases (
    case_id integer NOT NULL,
    case_number character varying(50) NOT NULL,
    title character varying(200) NOT NULL,
    description text,
    crime_type character varying(100) NOT NULL,
    case_opened_date date NOT NULL,
    case_closed_date date,
    status character varying(30) DEFAULT 'ongoing'::character varying,
    priority character varying(20),
    court_file_number character varying(50),
    sentence_years integer,
    sentence_months integer,
    fine_amount numeric(10,2),
    CONSTRAINT chk_priority CHECK (((priority)::text = ANY (ARRAY[('low'::character varying)::text, ('normal'::character varying)::text, ('high'::character varying)::text, ('urgent'::character varying)::text]))),
    CONSTRAINT chk_sentence_months CHECK (((sentence_months >= 0) AND (sentence_months < 12))),
    CONSTRAINT chk_sentence_years CHECK ((sentence_years >= 0)),
    CONSTRAINT chk_status CHECK (((status)::text = ANY (ARRAY[('ongoing'::character varying)::text, ('suspended'::character varying)::text, ('closed'::character varying)::text, ('dismissed'::character varying)::text, ('transferred'::character varying)::text])))
);


ALTER TABLE public.criminal_cases OWNER TO postgres;

--
-- Name: criminal_cases_case_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.criminal_cases_case_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.criminal_cases_case_id_seq OWNER TO postgres;

--
-- Name: criminal_cases_case_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.criminal_cases_case_id_seq OWNED BY public.criminal_cases.case_id;


--
-- Name: criminal_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.criminal_history (
    history_id integer NOT NULL,
    criminal_id integer NOT NULL,
    case_id integer,
    event_date date NOT NULL,
    event_description text NOT NULL,
    event_type character varying(50),
    location character varying(200),
    CONSTRAINT chk_event_type CHECK (((event_type)::text = ANY (ARRAY[('detention'::character varying)::text, ('arrest'::character varying)::text, ('conviction'::character varying)::text, ('release'::character varying)::text, ('escape'::character varying)::text, ('discipline'::character varying)::text, ('other'::character varying)::text])))
);


ALTER TABLE public.criminal_history OWNER TO postgres;

--
-- Name: criminal_history_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.criminal_history_history_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.criminal_history_history_id_seq OWNER TO postgres;

--
-- Name: criminal_history_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.criminal_history_history_id_seq OWNED BY public.criminal_history.history_id;


--
-- Name: criminals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.criminals (
    criminal_id integer NOT NULL,
    first_name character varying(50) NOT NULL,
    last_name character varying(50) NOT NULL,
    ssn character(11),
    date_of_birth date NOT NULL,
    gender character(1),
    citizenship character varying(50),
    home_address text,
    status character varying(30),
    arrest_date date,
    prison_id integer,
    cell_number character varying(20),
    threat_level character varying(20),
    CONSTRAINT chk_gender CHECK ((gender = ANY (ARRAY['M'::bpchar, 'F'::bpchar]))),
    CONSTRAINT chk_status CHECK (((status)::text = ANY (ARRAY[('arrested'::character varying)::text, ('imprisoned'::character varying)::text, ('released'::character varying)::text, ('wanted'::character varying)::text, ('suspended'::character varying)::text]))),
    CONSTRAINT chk_threat_level CHECK (((threat_level)::text = ANY (ARRAY[('low'::character varying)::text, ('medium'::character varying)::text, ('high'::character varying)::text, ('extreme'::character varying)::text])))
);


ALTER TABLE public.criminals OWNER TO postgres;

--
-- Name: criminals_criminal_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.criminals_criminal_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.criminals_criminal_id_seq OWNER TO postgres;

--
-- Name: criminals_criminal_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.criminals_criminal_id_seq OWNED BY public.criminals.criminal_id;


--
-- Name: officer_case_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.officer_case_assignments (
    id integer NOT NULL,
    officer_id integer NOT NULL,
    case_id integer NOT NULL,
    assignment_date date DEFAULT CURRENT_DATE,
    end_date date,
    role character varying(50),
    notes text,
    CONSTRAINT chk_assignment_role CHECK (((role)::text = ANY (ARRAY[('lead'::character varying)::text, ('assistant'::character varying)::text, ('supervisor'::character varying)::text, ('consultant'::character varying)::text])))
);


ALTER TABLE public.officer_case_assignments OWNER TO postgres;

--
-- Name: officer_case_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.officer_case_assignments_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.officer_case_assignments_id_seq OWNER TO postgres;

--
-- Name: officer_case_assignments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.officer_case_assignments_id_seq OWNED BY public.officer_case_assignments.id;


--
-- Name: officers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.officers (
    officer_id integer NOT NULL,
    first_name character varying(50) NOT NULL,
    last_name character varying(50) NOT NULL,
    ssn character(11) NOT NULL,
    date_of_birth date NOT NULL,
    rank character varying(50),
    specialization character varying(100),
    hire_date date NOT NULL,
    prison_id integer,
    phone character varying(15),
    email character varying(100),
    is_active boolean DEFAULT true
);


ALTER TABLE public.officers OWNER TO postgres;

--
-- Name: officers_officer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.officers_officer_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.officers_officer_id_seq OWNER TO postgres;

--
-- Name: officers_officer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.officers_officer_id_seq OWNED BY public.officers.officer_id;


--
-- Name: prison_incidents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prison_incidents (
    incident_id integer NOT NULL,
    prison_id integer NOT NULL,
    criminal_id integer,
    officer_id integer,
    incident_datetime timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    incident_type character varying(50),
    description text NOT NULL,
    severity character varying(20),
    CONSTRAINT chk_incident_type CHECK (((incident_type)::text = ANY (ARRAY[('fight'::character varying)::text, ('escape'::character varying)::text, ('escape_attempt'::character varying)::text, ('illness'::character varying)::text, ('self_harm'::character varying)::text, ('conflict'::character varying)::text, ('good_behavior'::character varying)::text, ('other'::character varying)::text]))),
    CONSTRAINT chk_severity CHECK (((severity)::text = ANY (ARRAY[('low'::character varying)::text, ('medium'::character varying)::text, ('high'::character varying)::text, ('critical'::character varying)::text])))
);


ALTER TABLE public.prison_incidents OWNER TO postgres;

--
-- Name: prison_incidents_incident_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.prison_incidents_incident_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.prison_incidents_incident_id_seq OWNER TO postgres;

--
-- Name: prison_incidents_incident_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.prison_incidents_incident_id_seq OWNED BY public.prison_incidents.incident_id;


--
-- Name: prison_visits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prison_visits (
    visit_id integer NOT NULL,
    criminal_id integer NOT NULL,
    prison_id integer NOT NULL,
    visit_datetime timestamp without time zone NOT NULL,
    visitor_first_name character varying(50) NOT NULL,
    visitor_last_name character varying(50) NOT NULL,
    visitor_ssn character(11),
    relationship character varying(50),
    duration_minutes integer,
    is_approved boolean DEFAULT false,
    notes text,
    CONSTRAINT chk_duration CHECK ((duration_minutes > 0))
);


ALTER TABLE public.prison_visits OWNER TO postgres;

--
-- Name: prison_visits_visit_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.prison_visits_visit_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.prison_visits_visit_id_seq OWNER TO postgres;

--
-- Name: prison_visits_visit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.prison_visits_visit_id_seq OWNED BY public.prison_visits.visit_id;


--
-- Name: prisons; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.prisons (
    prison_id integer NOT NULL,
    name character varying(100) NOT NULL,
    location character varying(200) NOT NULL,
    capacity integer NOT NULL,
    security_level character varying(20),
    opening_date date,
    number_of_cells integer,
    is_active boolean DEFAULT true,
    CONSTRAINT chk_capacity CHECK ((capacity > 0)),
    CONSTRAINT chk_security_level CHECK (((security_level)::text = ANY (ARRAY[('low'::character varying)::text, ('medium'::character varying)::text, ('high'::character varying)::text, ('maximum'::character varying)::text])))
);


ALTER TABLE public.prisons OWNER TO postgres;

--
-- Name: prisons_prison_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.prisons_prison_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.prisons_prison_id_seq OWNER TO postgres;

--
-- Name: prisons_prison_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.prisons_prison_id_seq OWNED BY public.prisons.prison_id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(20) DEFAULT 'USER'::character varying NOT NULL
);

-- INSERT INTO public.users (id, username, email, password, role) 
-- VALUES (1, 'admin', 'admin@test.pl', '$2a$10$7jHEOMB/8S/VDNIa74n16eVsktFZD4IoKWCa2ItHX3zqnl7JvEi0.', 'ADMIN');
ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: view_active_cases; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_active_cases AS
 SELECT cc.case_id,
    cc.case_number,
    cc.title,
    cc.crime_type,
    cc.status,
    cc.priority,
    cc.case_opened_date,
    string_agg(DISTINCT ((((((o.first_name)::text || ' '::text) || (o.last_name)::text) || ' ('::text) || (oca.role)::text) || ')'::text), ', '::text) AS assigned_officers,
    count(DISTINCT cr.criminal_id) AS number_of_suspects
   FROM ((((public.criminal_cases cc
     LEFT JOIN public.officer_case_assignments oca ON ((cc.case_id = oca.case_id)))
     LEFT JOIN public.officers o ON ((oca.officer_id = o.officer_id)))
     LEFT JOIN public.case_criminals ccr ON ((cc.case_id = ccr.case_id)))
     LEFT JOIN public.criminals cr ON ((ccr.criminal_id = cr.criminal_id)))
  WHERE ((cc.status)::text = ANY (ARRAY[('ongoing'::character varying)::text, ('suspended'::character varying)::text]))
  GROUP BY cc.case_id, cc.case_number, cc.title, cc.crime_type, cc.status, cc.priority, cc.case_opened_date
  ORDER BY cc.priority DESC, cc.case_opened_date;


ALTER VIEW public.view_active_cases OWNER TO postgres;

--
-- Name: view_criminal_profiles; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_criminal_profiles AS
 SELECT c.criminal_id,
    (((c.first_name)::text || ' '::text) || (c.last_name)::text) AS full_name,
    c.date_of_birth,
    c.gender,
    c.citizenship,
    c.status,
    c.threat_level,
    p.name AS current_prison,
    c.cell_number,
    count(DISTINCT ch.history_id) AS history_events,
    count(DISTINCT ccr.case_id) AS related_cases
   FROM (((public.criminals c
     LEFT JOIN public.prisons p ON ((c.prison_id = p.prison_id)))
     LEFT JOIN public.criminal_history ch ON ((c.criminal_id = ch.criminal_id)))
     LEFT JOIN public.case_criminals ccr ON ((c.criminal_id = ccr.criminal_id)))
  GROUP BY c.criminal_id, c.first_name, c.last_name, c.date_of_birth, c.gender, c.citizenship, c.status, c.threat_level, p.name, c.cell_number
  ORDER BY c.threat_level DESC, c.last_name;


ALTER VIEW public.view_criminal_profiles OWNER TO postgres;

--
-- Name: view_officer_workload; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_officer_workload AS
 SELECT o.officer_id,
    (((o.first_name)::text || ' '::text) || (o.last_name)::text) AS officer_name,
    o.rank,
    o.specialization,
    p.name AS assigned_prison,
    count(DISTINCT oca.case_id) AS active_cases,
    string_agg(DISTINCT (oca.role)::text, ', '::text) AS roles
   FROM (((public.officers o
     LEFT JOIN public.prisons p ON ((o.prison_id = p.prison_id)))
     LEFT JOIN public.officer_case_assignments oca ON ((o.officer_id = oca.officer_id)))
     LEFT JOIN public.criminal_cases cc ON (((oca.case_id = cc.case_id) AND ((cc.status)::text = 'ongoing'::text))))
  WHERE (o.is_active = true)
  GROUP BY o.officer_id, o.first_name, o.last_name, o.rank, o.specialization, p.name
  ORDER BY (count(DISTINCT oca.case_id)) DESC;


ALTER VIEW public.view_officer_workload OWNER TO postgres;

--
-- Name: view_prison_occupancy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_prison_occupancy AS
 SELECT p.prison_id,
    p.name AS prison_name,
    p.location,
    p.capacity,
    p.security_level,
    count(c.criminal_id) AS current_inmates,
    round((((count(c.criminal_id))::numeric / (p.capacity)::numeric) * (100)::numeric), 2) AS occupancy_percentage,
    (p.capacity - count(c.criminal_id)) AS available_cells
   FROM (public.prisons p
     LEFT JOIN public.criminals c ON (((p.prison_id = c.prison_id) AND ((c.status)::text = 'imprisoned'::text))))
  WHERE (p.is_active = true)
  GROUP BY p.prison_id, p.name, p.location, p.capacity, p.security_level
  ORDER BY (round((((count(c.criminal_id))::numeric / (p.capacity)::numeric) * (100)::numeric), 2)) DESC;


ALTER VIEW public.view_prison_occupancy OWNER TO postgres;

--
-- Name: view_recent_incidents; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.view_recent_incidents AS
 SELECT pi.incident_id,
    pi.incident_datetime,
    pr.name AS prison_name,
    pi.incident_type,
    pi.severity,
    (((c.first_name)::text || ' '::text) || (c.last_name)::text) AS criminal_involved,
    (((o.first_name)::text || ' '::text) || (o.last_name)::text) AS officer_involved,
    pi.description
   FROM (((public.prison_incidents pi
     JOIN public.prisons pr ON ((pi.prison_id = pr.prison_id)))
     LEFT JOIN public.criminals c ON ((pi.criminal_id = c.criminal_id)))
     LEFT JOIN public.officers o ON ((pi.officer_id = o.officer_id)))
  ORDER BY pi.incident_datetime DESC
 LIMIT 50;


ALTER VIEW public.view_recent_incidents OWNER TO postgres;

--
-- Name: case_criminals id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.case_criminals ALTER COLUMN id SET DEFAULT nextval('public.case_criminals_id_seq'::regclass);


--
-- Name: criminal_cases case_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_cases ALTER COLUMN case_id SET DEFAULT nextval('public.criminal_cases_case_id_seq'::regclass);


--
-- Name: criminal_history history_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_history ALTER COLUMN history_id SET DEFAULT nextval('public.criminal_history_history_id_seq'::regclass);


--
-- Name: criminals criminal_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminals ALTER COLUMN criminal_id SET DEFAULT nextval('public.criminals_criminal_id_seq'::regclass);


--
-- Name: officer_case_assignments id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officer_case_assignments ALTER COLUMN id SET DEFAULT nextval('public.officer_case_assignments_id_seq'::regclass);


--
-- Name: officers officer_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officers ALTER COLUMN officer_id SET DEFAULT nextval('public.officers_officer_id_seq'::regclass);


--
-- Name: prison_incidents incident_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_incidents ALTER COLUMN incident_id SET DEFAULT nextval('public.prison_incidents_incident_id_seq'::regclass);


--
-- Name: prison_visits visit_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_visits ALTER COLUMN visit_id SET DEFAULT nextval('public.prison_visits_visit_id_seq'::regclass);


--
-- Name: prisons prison_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prisons ALTER COLUMN prison_id SET DEFAULT nextval('public.prisons_prison_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: case_criminals; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.case_criminals (id, case_id, criminal_id, role, assignment_date, notes) FROM stdin;
1	1	1	convicted	2023-03-15	Main perpetrator, armed with handgun
2	2	2	accused	2023-06-20	Courier in drug trafficking operation
3	3	3	convicted	2022-11-10	Convicted of first-degree murder
4	4	4	suspect	2024-01-15	Under investigation for multiple burglaries
5	5	5	convicted	2021-05-20	Mastermind of pyramid scheme
6	6	6	convicted	2023-09-10	Bar fight resulting in injuries
7	1	2	witness	2023-03-16	Witnessed the robbery as bystander
8	2	7	suspect	2023-06-18	International connection, still at large
\.


--
-- Data for Name: criminal_cases; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.criminal_cases (case_id, case_number, title, description, crime_type, case_opened_date, case_closed_date, status, priority, court_file_number, sentence_years, sentence_months, fine_amount) FROM stdin;
1	KRA/2023/1234	Armed Robbery	Bank robbery in downtown Krakow with firearms	Robbery	2023-03-10	\N	ongoing	high	II K 234/23	10	0	50000.00
2	WAW/2023/5678	Drug Trafficking	International organized crime group trafficking narcotics	Narcotics	2023-06-15	\N	ongoing	urgent	V K 567/23	8	6	100000.00
3	KAT/2022/9012	Murder	Premeditated murder with aggravating circumstances	Murder	2022-11-05	2023-05-20	closed	urgent	III K 901/22	25	0	\N
4	WAW/2024/3456	Burglary	Series of apartment break-ins in residential area	Theft	2024-01-10	\N	ongoing	normal	IV K 345/24	3	0	20000.00
5	KRA/2021/7890	Financial Fraud	Large-scale pyramid scheme investment fraud	Fraud	2021-05-15	2022-08-30	closed	high	I K 789/21	5	6	500000.00
6	GDA/2023/4567	Assault	Aggravated assault with bodily harm	Assault	2023-09-05	\N	ongoing	normal	VI K 456/23	2	0	15000.00
7	POZ/2023/8901	Vandalism	Destruction of public property	Vandalism	2023-08-20	2024-01-10	closed	low	VII K 890/23	0	6	5000.00
\.


--
-- Data for Name: criminal_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.criminal_history (history_id, criminal_id, case_id, event_date, event_description, event_type, location) FROM stdin;
1	1	1	2023-03-15	Arrested after high-speed chase through city center	arrest	Krakow, Dluga Street 15
2	1	1	2023-08-20	Court sentenced to 10 years imprisonment	conviction	District Court in Krakow
3	2	2	2023-06-20	Detained at airport with suspicious package	detention	Warsaw Chopin Airport
4	3	3	2022-11-10	Detained at crime scene with murder weapon	arrest	Katowice, Kosciuszki Street 8
5	3	3	2023-02-15	Court sentenced to 25 years imprisonment	conviction	District Court in Katowice
6	4	4	2024-01-15	Arrested during burglary attempt	arrest	Warsaw, Pulaskiego Street 42
7	5	5	2021-05-20	Arrested at company headquarters	arrest	Gdansk, Dluga 33
8	5	5	2022-03-10	Convicted of large-scale fraud	conviction	District Court in Gdansk
9	6	6	2023-09-10	Detained after bar incident	detention	Poznan, Polwiejska 15
10	6	6	2023-11-22	Sentenced to community service	conviction	Municipal Court in Poznan
11	8	\N	2023-01-20	Released after serving full sentence	release	Warsaw Central Prison
\.


--
-- Data for Name: criminals; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.criminals (criminal_id, first_name, last_name, ssn, date_of_birth, gender, citizenship, home_address, status, arrest_date, prison_id, cell_number, threat_level) FROM stdin;
1	Thomas	Black	88050156789	1988-05-01	M	Poland	Warsaw, Targowa 15/3	imprisoned	2023-03-15	1	A-101	high
2	Martin	Grey	90081234567	1990-08-12	M	Poland	Krakow, Florianska 22/8	imprisoned	2023-06-20	1	B-205	medium
3	Christopher	Brown	85030398765	1985-03-03	M	Poland	Katowice, Staromiejska 7	imprisoned	2022-11-10	2	C-310	extreme
4	Andrew	Green	92120567890	1992-12-05	M	Ukraine	Lviv, Shevchenka 45	arrested	2024-01-15	3	D-115	medium
5	Robert	White	87070712345	1987-07-07	M	Poland	Gdansk, Dluga 33/12	imprisoned	2021-05-20	2	A-220	high
6	James	Stone	93041234567	1993-04-12	M	Poland	Poznan, Paderewskiego 18	imprisoned	2023-09-10	4	B-110	low
7	Daniel	Silver	89060876543	1989-06-08	M	Germany	Berlin, Hauptstrasse 56	wanted	\N	\N	\N	high
8	Alex	Gold	91110298765	1991-11-02	M	Poland	Warsaw, Marszalkowska 99	released	2020-03-05	\N	\N	low
\.


--
-- Data for Name: officer_case_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.officer_case_assignments (id, officer_id, case_id, assignment_date, end_date, role, notes) FROM stdin;
1	1	1	2023-03-10	\N	lead	Lead investigator on armed robbery case
2	2	1	2023-03-12	2023-08-15	assistant	Assisted with witness interviews
3	3	2	2023-06-15	\N	lead	Leading drug trafficking investigation
4	4	3	2022-11-05	2023-05-20	consultant	Psychological profile of suspect
5	5	4	2024-01-10	\N	supervisor	Supervising burglary investigation
6	6	5	2021-05-15	2022-08-30	assistant	Document analysis and forensics
7	1	6	2023-09-05	\N	lead	Investigation of assault case
8	7	2	2023-06-20	\N	supervisor	Coordinating international cooperation
\.


--
-- Data for Name: officers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.officers (officer_id, first_name, last_name, ssn, date_of_birth, rank, specialization, hire_date, prison_id, phone, email, is_active) FROM stdin;
1	John	Smith	75032145678	1975-03-21	Chief Superintendent	Security	2000-05-10	1	512345678	j.smith@prison.gov	t
2	Anna	Johnson	82071234567	1982-07-12	Superintendent	Rehabilitation	2005-09-01	1	523456789	a.johnson@prison.gov	t
3	Peter	Williams	78110298765	1978-11-02	Inspector	Security	2003-03-15	2	534567890	p.williams@prison.gov	t
4	Katherine	Brown	85040512345	1985-04-05	Officer	Psychology	2010-01-20	2	545678901	k.brown@prison.gov	t
5	Mark	Davis	70092387654	1970-09-23	Senior Inspector	Administration	1995-07-01	3	556789012	m.davis@prison.gov	t
6	Sarah	Wilson	88053176543	1988-05-31	Lieutenant	Training	2012-03-15	1	567890123	s.wilson@prison.gov	t
7	Michael	Taylor	79081287654	1979-08-12	Captain	Operations	2008-06-20	3	578901234	m.taylor@prison.gov	t
8	Emma	Anderson	91020398765	1991-02-03	Officer	Medical	2015-11-10	4	589012345	e.anderson@prison.gov	t
\.


--
-- Data for Name: prison_incidents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prison_incidents (incident_id, prison_id, criminal_id, officer_id, incident_datetime, incident_type, description, severity) FROM stdin;
1	1	1	1	2023-09-22 21:30:00	fight	Physical altercation in cafeteria between two inmates	medium
2	2	3	3	2023-04-15 03:15:00	escape_attempt	Attempted to cut through cell bars using smuggled tool	critical
3	1	2	6	2023-08-10 15:45:00	good_behavior	Helped defuse tense situation between inmates	low
4	3	4	5	2024-01-20 08:30:00	illness	Inmate reported severe stomach pain, transported to medical	medium
5	2	5	3	2022-03-08 19:00:00	conflict	Verbal argument with guard during evening count	low
6	4	6	8	2023-10-12 12:00:00	self_harm	Minor self-inflicted cuts, psychological evaluation ordered	high
7	1	\N	1	2023-11-05 02:00:00	other	Suspicious activity detected on perimeter cameras	medium
8	2	3	\N	2023-07-18 16:30:00	fight	Assault on another inmate in recreation yard	high
\.


--
-- Data for Name: prison_visits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prison_visits (visit_id, criminal_id, prison_id, visit_datetime, visitor_first_name, visitor_last_name, visitor_ssn, relationship, duration_minutes, is_approved, notes) FROM stdin;
1	1	1	2023-09-15 14:00:00	Mary	Black	90041234567	Wife	45	t	Regular family visit
2	1	1	2023-10-20 15:30:00	David	Black	12050198765	Father	30	t	Birthday visit
3	2	1	2023-07-10 10:00:00	Susan	Grey	88091287654	Mother	60	t	\N
4	3	2	2023-03-25 13:00:00	Patricia	Brown	86070176543	Sister	40	t	Emotional support visit
5	5	2	2021-08-15 11:00:00	Richard	White	85060212345	Brother	50	t	Legal consultation present
6	6	4	2023-10-05 14:30:00	Jennifer	Stone	94051298765	Girlfriend	45	t	\N
7	1	1	2024-01-10 09:00:00	John	Kowalski	75032167890	Lawyer	90	t	Legal consultation
8	3	2	2023-06-18 10:30:00	Anna	Nowak	82111234567	Priest	35	t	Religious counseling
\.


--
-- Data for Name: prisons; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.prisons (prison_id, name, location, capacity, security_level, opening_date, number_of_cells, is_active) FROM stdin;
1	Warsaw Central Prison	Warsaw, Marywilska Street 19	800	medium	1995-03-15	400	t
2	Krakow Maximum Security	Krakow, Zgody Estate 5	500	high	1988-06-20	250	t
3	Katowice Detention Center	Katowice, Mikolowska Street 64	350	maximum	2001-11-10	175	t
4	Gdansk Correctional Facility	Gdansk, Portowa Street 88	600	medium	1998-09-05	300	t
5	Poznan Youth Detention	Poznan, Slowackiego Avenue 12	200	low	2010-01-20	100	t
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, username, email, password, role) FROM stdin;
1	admin	admin@example.com	$2a$10$7jHEOMB/8S/VDNIa74n16eVsktFZD4IoKWCa2ItHX3zqnl7JvEi0.	admin
13	admin2	admin@gmail.com	$2a$10$3ujhWB66YXdM./Zs/fMT/OXlBQh/95HXB1nPOpFGJnXcgLJlf.NV2	admin
14	user	user@mail.com	$2a$10$jbUMxc7e5tgXUqg37pWQ3eMDhtNtmomGzNaf9gtWduz55dpmGnIym	user
\.


--
-- Name: case_criminals_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.case_criminals_id_seq', 8, true);


--
-- Name: criminal_cases_case_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.criminal_cases_case_id_seq', 7, true);


--
-- Name: criminal_history_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.criminal_history_history_id_seq', 11, true);


--
-- Name: criminals_criminal_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.criminals_criminal_id_seq', 8, true);


--
-- Name: officer_case_assignments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.officer_case_assignments_id_seq', 8, true);


--
-- Name: officers_officer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.officers_officer_id_seq', 8, true);


--
-- Name: prison_incidents_incident_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.prison_incidents_incident_id_seq', 8, true);


--
-- Name: prison_visits_visit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.prison_visits_visit_id_seq', 8, true);


--
-- Name: prisons_prison_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.prisons_prison_id_seq', 6, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 15, false);


--
-- Name: case_criminals pk_case_criminals; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.case_criminals
    ADD CONSTRAINT pk_case_criminals PRIMARY KEY (id);


--
-- Name: criminal_cases pk_criminal_cases; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_cases
    ADD CONSTRAINT pk_criminal_cases PRIMARY KEY (case_id);


--
-- Name: criminal_history pk_criminal_history; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_history
    ADD CONSTRAINT pk_criminal_history PRIMARY KEY (history_id);


--
-- Name: criminals pk_criminals; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminals
    ADD CONSTRAINT pk_criminals PRIMARY KEY (criminal_id);


--
-- Name: officer_case_assignments pk_officer_case_assignments; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officer_case_assignments
    ADD CONSTRAINT pk_officer_case_assignments PRIMARY KEY (id);


--
-- Name: officers pk_officers; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officers
    ADD CONSTRAINT pk_officers PRIMARY KEY (officer_id);


--
-- Name: prison_incidents pk_prison_incidents; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_incidents
    ADD CONSTRAINT pk_prison_incidents PRIMARY KEY (incident_id);


--
-- Name: prison_visits pk_prison_visits; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_visits
    ADD CONSTRAINT pk_prison_visits PRIMARY KEY (visit_id);


--
-- Name: prisons pk_prisons; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prisons
    ADD CONSTRAINT pk_prisons PRIMARY KEY (prison_id);


--
-- Name: case_criminals uk_case_criminal; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.case_criminals
    ADD CONSTRAINT uk_case_criminal UNIQUE (case_id, criminal_id);


--
-- Name: criminal_cases uk_case_number; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_cases
    ADD CONSTRAINT uk_case_number UNIQUE (case_number);


--
-- Name: criminals uk_criminals_ssn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminals
    ADD CONSTRAINT uk_criminals_ssn UNIQUE (ssn);


--
-- Name: officer_case_assignments uk_officer_case_role; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officer_case_assignments
    ADD CONSTRAINT uk_officer_case_role UNIQUE (officer_id, case_id, role);


--
-- Name: officers uk_officers_ssn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officers
    ADD CONSTRAINT uk_officers_ssn UNIQUE (ssn);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: idx_cases_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cases_date ON public.criminal_cases USING btree (case_opened_date);


--
-- Name: idx_cases_priority; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cases_priority ON public.criminal_cases USING btree (priority);


--
-- Name: idx_cases_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cases_status ON public.criminal_cases USING btree (status);


--
-- Name: idx_cc_case; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cc_case ON public.case_criminals USING btree (case_id);


--
-- Name: idx_cc_criminal; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_cc_criminal ON public.case_criminals USING btree (criminal_id);


--
-- Name: idx_criminals_prison; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_criminals_prison ON public.criminals USING btree (prison_id);


--
-- Name: idx_criminals_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_criminals_status ON public.criminals USING btree (status);


--
-- Name: idx_criminals_threat; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_criminals_threat ON public.criminals USING btree (threat_level);


--
-- Name: idx_history_criminal; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_history_criminal ON public.criminal_history USING btree (criminal_id);


--
-- Name: idx_history_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_history_date ON public.criminal_history USING btree (event_date);


--
-- Name: idx_incidents_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_incidents_date ON public.prison_incidents USING btree (incident_datetime);


--
-- Name: idx_incidents_prison; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_incidents_prison ON public.prison_incidents USING btree (prison_id);


--
-- Name: idx_oca_case; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_oca_case ON public.officer_case_assignments USING btree (case_id);


--
-- Name: idx_oca_officer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_oca_officer ON public.officer_case_assignments USING btree (officer_id);


--
-- Name: idx_officers_active; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_officers_active ON public.officers USING btree (is_active);


--
-- Name: idx_officers_prison; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_officers_prison ON public.officers USING btree (prison_id);


--
-- Name: idx_visits_criminal; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visits_criminal ON public.prison_visits USING btree (criminal_id);


--
-- Name: idx_visits_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visits_date ON public.prison_visits USING btree (visit_datetime);


--
-- Name: idx_visits_prison; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_visits_prison ON public.prison_visits USING btree (prison_id);


--
-- Name: case_criminals fk_cc_case; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.case_criminals
    ADD CONSTRAINT fk_cc_case FOREIGN KEY (case_id) REFERENCES public.criminal_cases(case_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: case_criminals fk_cc_criminal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.case_criminals
    ADD CONSTRAINT fk_cc_criminal FOREIGN KEY (criminal_id) REFERENCES public.criminals(criminal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: criminals fk_criminals_prison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminals
    ADD CONSTRAINT fk_criminals_prison FOREIGN KEY (prison_id) REFERENCES public.prisons(prison_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: criminal_history fk_history_case; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_history
    ADD CONSTRAINT fk_history_case FOREIGN KEY (case_id) REFERENCES public.criminal_cases(case_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: criminal_history fk_history_criminal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.criminal_history
    ADD CONSTRAINT fk_history_criminal FOREIGN KEY (criminal_id) REFERENCES public.criminals(criminal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: prison_incidents fk_incidents_criminal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_incidents
    ADD CONSTRAINT fk_incidents_criminal FOREIGN KEY (criminal_id) REFERENCES public.criminals(criminal_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: prison_incidents fk_incidents_officer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_incidents
    ADD CONSTRAINT fk_incidents_officer FOREIGN KEY (officer_id) REFERENCES public.officers(officer_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: prison_incidents fk_incidents_prison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_incidents
    ADD CONSTRAINT fk_incidents_prison FOREIGN KEY (prison_id) REFERENCES public.prisons(prison_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: officer_case_assignments fk_oca_case; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officer_case_assignments
    ADD CONSTRAINT fk_oca_case FOREIGN KEY (case_id) REFERENCES public.criminal_cases(case_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: officer_case_assignments fk_oca_officer; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officer_case_assignments
    ADD CONSTRAINT fk_oca_officer FOREIGN KEY (officer_id) REFERENCES public.officers(officer_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: officers fk_officers_prison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.officers
    ADD CONSTRAINT fk_officers_prison FOREIGN KEY (prison_id) REFERENCES public.prisons(prison_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Name: prison_visits fk_visits_criminal; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_visits
    ADD CONSTRAINT fk_visits_criminal FOREIGN KEY (criminal_id) REFERENCES public.criminals(criminal_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: prison_visits fk_visits_prison; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.prison_visits
    ADD CONSTRAINT fk_visits_prison FOREIGN KEY (prison_id) REFERENCES public.prisons(prison_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict VVPooWBFIwBeaRARrYBea5m9luG7lwjWejelSTotLUqeZAmpj1LphH49aVbIjnl

