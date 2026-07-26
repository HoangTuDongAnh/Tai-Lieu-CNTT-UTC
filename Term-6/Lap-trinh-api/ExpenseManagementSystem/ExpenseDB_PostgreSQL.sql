--
-- PostgreSQL database dump
--

\restrict iVia5K5cVEIsomKLcOwbSR5jCieJ0N4k5BLG5nUmgv5nCeZs0o5bHFH1jXsqdiC

-- Dumped from database version 18.3 (Debian 18.3-1.pgdg12+1)
-- Dumped by pg_dump version 18.3

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

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: mimodb
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO mimodb;

--
-- Name: fn_createdefaultwallet(); Type: FUNCTION; Schema: public; Owner: mimodb
--

CREATE FUNCTION public.fn_createdefaultwallet() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO public.wallets (
        walletid, userid, walletname, initialbalance, 
        currentbalance, currency, isdefault, createdat, updatedat
    ) VALUES (
        public.fn_GenerateWalletID(),
        NEW.userid,
        'Tài khoản chính',
        0, 0, 'VND', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.fn_createdefaultwallet() OWNER TO mimodb;

--
-- Name: fn_generatesupportrequestid(); Type: FUNCTION; Schema: public; Owner: mimodb
--

CREATE FUNCTION public.fn_generatesupportrequestid() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    Prefix VARCHAR(8) := 'SR' || to_char(CURRENT_DATE, 'YYMMDD');
    LastSeq INT;
BEGIN
    SELECT COALESCE(MAX(RIGHT(SupportRequestID, 6)::INT), 0)
    INTO LastSeq
    FROM SupportRequests
    WHERE SupportRequestID LIKE Prefix || '%';
    RETURN Prefix || lpad((LastSeq + 1)::TEXT, 6, '0');
END; $$;


ALTER FUNCTION public.fn_generatesupportrequestid() OWNER TO mimodb;

--
-- Name: fn_generateuserid(); Type: FUNCTION; Schema: public; Owner: mimodb
--

CREATE FUNCTION public.fn_generateuserid() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    Prefix VARCHAR(7) := 'U' || to_char(CURRENT_DATE, 'YYMMDD');
    LastSeq INT;
BEGIN
    SELECT COALESCE(MAX(SUBSTRING(UserID FROM 8)::INT), 0) INTO LastSeq FROM Users WHERE UserID LIKE Prefix || '%';
    RETURN Prefix || lpad((LastSeq + 1)::TEXT, 4, '0');
END; $$;


ALTER FUNCTION public.fn_generateuserid() OWNER TO mimodb;

--
-- Name: fn_generatewalletid(); Type: FUNCTION; Schema: public; Owner: mimodb
--

CREATE FUNCTION public.fn_generatewalletid() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    Prefix VARCHAR(7) := 'W' || to_char(CURRENT_DATE, 'YYMMDD');
BEGIN
    -- W + YYMMDD (7) + 5 số = 12 ký tự
    RETURN Prefix || lpad(nextval('public.seq_walletid')::TEXT, 5, '0');
END; $$;


ALTER FUNCTION public.fn_generatewalletid() OWNER TO mimodb;

--
-- Name: fn_setdefaultavatar(); Type: FUNCTION; Schema: public; Owner: mimodb
--

CREATE FUNCTION public.fn_setdefaultavatar() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.Avatar IS NULL OR NEW.Avatar = '' THEN
        NEW.Avatar := '/sneat/img/avatars/default/teams_1.png';
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.fn_setdefaultavatar() OWNER TO mimodb;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: budgets; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.budgets (
    budgetid character varying(13) NOT NULL,
    userid character varying(15) NOT NULL,
    categoryid character varying(15) NOT NULL,
    limitamount numeric(15,2) NOT NULL,
    spentamount numeric(15,2) DEFAULT 0 NOT NULL,
    periodtype character varying(10) NOT NULL,
    periodyear smallint NOT NULL,
    periodmonth smallint,
    periodweek smallint,
    startdate date NOT NULL,
    enddate date NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    periodmonth_nonull smallint GENERATED ALWAYS AS (COALESCE((periodmonth)::integer, 0)) STORED,
    periodweek_nonull smallint GENERATED ALWAYS AS (COALESCE((periodweek)::integer, 0)) STORED,
    CONSTRAINT budgets_limitamount_check CHECK ((limitamount > (0)::numeric)),
    CONSTRAINT budgets_periodmonth_check CHECK (((periodmonth IS NULL) OR ((periodmonth >= 1) AND (periodmonth <= 12)))),
    CONSTRAINT budgets_periodtype_check CHECK (((periodtype)::text = ANY ((ARRAY['week'::character varying, 'month'::character varying, 'year'::character varying])::text[]))),
    CONSTRAINT budgets_periodweek_check CHECK (((periodweek IS NULL) OR ((periodweek >= 1) AND (periodweek <= 53)))),
    CONSTRAINT budgets_periodyear_check CHECK (((periodyear >= 2000) AND (periodyear <= 9999))),
    CONSTRAINT budgets_spentamount_check CHECK ((spentamount >= (0)::numeric)),
    CONSTRAINT ck_budgets_daterange CHECK ((startdate <= enddate)),
    CONSTRAINT ck_budgets_periodshape CHECK (((((periodtype)::text = 'week'::text) AND (periodweek IS NOT NULL) AND (periodmonth IS NULL)) OR (((periodtype)::text = 'month'::text) AND (periodmonth IS NOT NULL) AND (periodweek IS NULL)) OR (((periodtype)::text = 'year'::text) AND (periodmonth IS NULL) AND (periodweek IS NULL))))
);


ALTER TABLE public.budgets OWNER TO mimodb;

--
-- Name: categories; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.categories (
    categoryid character varying(15) NOT NULL,
    userid character varying(15),
    categoryname character varying(100) NOT NULL,
    icon character varying(255),
    color character varying(10),
    isdefault boolean DEFAULT false NOT NULL,
    isdeleted boolean DEFAULT false NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    categorytype character varying(10) DEFAULT 'expense'::character varying NOT NULL,
    CONSTRAINT categories_categorytype_check CHECK (((categorytype)::text = ANY ((ARRAY['expense'::character varying, 'income'::character varying])::text[])))
);


ALTER TABLE public.categories OWNER TO mimodb;

--
-- Name: seq_walletid; Type: SEQUENCE; Schema: public; Owner: mimodb
--

CREATE SEQUENCE public.seq_walletid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.seq_walletid OWNER TO mimodb;

--
-- Name: supportattachments; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.supportattachments (
    attachmentid integer NOT NULL,
    supportrequestid character varying(18) NOT NULL,
    filename character varying(255) NOT NULL,
    fileurl character varying(500) NOT NULL,
    filetype character varying(100),
    filesize bigint,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.supportattachments OWNER TO mimodb;

--
-- Name: supportattachments_attachmentid_seq; Type: SEQUENCE; Schema: public; Owner: mimodb
--

CREATE SEQUENCE public.supportattachments_attachmentid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.supportattachments_attachmentid_seq OWNER TO mimodb;

--
-- Name: supportattachments_attachmentid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mimodb
--

ALTER SEQUENCE public.supportattachments_attachmentid_seq OWNED BY public.supportattachments.attachmentid;


--
-- Name: supportrequests; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.supportrequests (
    supportrequestid character varying(17) NOT NULL,
    userid character varying(15) NOT NULL,
    subject character varying(200) NOT NULL,
    message character varying(2000) NOT NULL,
    supporttype character varying(20) NOT NULL,
    priority character varying(10) NOT NULL,
    status character varying(20) NOT NULL,
    adminreply character varying(2000),
    createdat timestamp without time zone DEFAULT now() NOT NULL,
    updatedat timestamp without time zone DEFAULT now() NOT NULL,
    viewedat timestamp without time zone,
    repliedat timestamp without time zone,
    closedat timestamp without time zone
);


ALTER TABLE public.supportrequests OWNER TO mimodb;

--
-- Name: transactions; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.transactions (
    transactionid character varying(17) NOT NULL,
    userid character varying(15) NOT NULL,
    walletid character varying(12) NOT NULL,
    categoryid character varying(15) NOT NULL,
    transactiontype character varying(10) NOT NULL,
    amount numeric(15,2) NOT NULL,
    transactiondate date NOT NULL,
    note text,
    isrecurring boolean DEFAULT false NOT NULL,
    recurinterval character varying(20),
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT transactions_amount_check CHECK ((amount > (0)::numeric)),
    CONSTRAINT transactions_recurinterval_check CHECK (((recurinterval IS NULL) OR ((recurinterval)::text = ANY ((ARRAY['daily'::character varying, 'weekly'::character varying, 'monthly'::character varying, 'yearly'::character varying])::text[])))),
    CONSTRAINT transactions_transactiontype_check CHECK (((transactiontype)::text = ANY ((ARRAY['expense'::character varying, 'income'::character varying])::text[])))
);


ALTER TABLE public.transactions OWNER TO mimodb;

--
-- Name: userotps; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.userotps (
    otpid integer NOT NULL,
    email character varying(150) NOT NULL,
    otpcode character varying(6) NOT NULL,
    isused boolean DEFAULT false NOT NULL,
    expiresat timestamp without time zone NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.userotps OWNER TO mimodb;

--
-- Name: userotps_otpid_seq; Type: SEQUENCE; Schema: public; Owner: mimodb
--

CREATE SEQUENCE public.userotps_otpid_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.userotps_otpid_seq OWNER TO mimodb;

--
-- Name: userotps_otpid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mimodb
--

ALTER SEQUENCE public.userotps_otpid_seq OWNED BY public.userotps.otpid;


--
-- Name: users; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.users (
    userid character varying(15) NOT NULL,
    fullname character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    passwordhash character varying(255) NOT NULL,
    phonenumber character varying(15),
    avatar character varying(255),
    role character varying(20) DEFAULT 'user'::character varying NOT NULL,
    status character varying(10) DEFAULT 'active'::character varying NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['user'::character varying, 'admin'::character varying])::text[]))),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['active'::character varying, 'inactive'::character varying, 'locked'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO mimodb;

--
-- Name: vw_categorybudgetoverview; Type: VIEW; Schema: public; Owner: mimodb
--

CREATE VIEW public.vw_categorybudgetoverview AS
 SELECT c.categoryid,
    c.userid,
    c.categoryname,
    c.icon,
    c.color,
    c.isdefault,
    c.isdeleted,
    b.budgetid,
    b.limitamount,
    b.spentamount,
    b.periodtype,
    b.startdate,
    b.enddate,
        CASE
            WHEN (b.budgetid IS NULL) THEN NULL::numeric
            ELSE (b.limitamount - b.spentamount)
        END AS remainingamount,
        CASE
            WHEN ((b.budgetid IS NULL) OR (b.limitamount = (0)::numeric)) THEN NULL::numeric
            ELSE round(((b.spentamount * 100.0) / b.limitamount), 2)
        END AS percentageused
   FROM (public.categories c
     LEFT JOIN public.budgets b ON (((c.categoryid)::text = (b.categoryid)::text)));


ALTER VIEW public.vw_categorybudgetoverview OWNER TO mimodb;

--
-- Name: wallets; Type: TABLE; Schema: public; Owner: mimodb
--

CREATE TABLE public.wallets (
    walletid character varying(12) NOT NULL,
    userid character varying(15) NOT NULL,
    walletname character varying(100) NOT NULL,
    initialbalance numeric(15,2) DEFAULT 0 NOT NULL,
    currentbalance numeric(15,2) DEFAULT 0 NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    isdefault boolean DEFAULT false NOT NULL,
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT wallets_currentbalance_check CHECK ((currentbalance >= (0)::numeric)),
    CONSTRAINT wallets_initialbalance_check CHECK ((initialbalance >= (0)::numeric))
);


ALTER TABLE public.wallets OWNER TO mimodb;

--
-- Name: supportattachments attachmentid; Type: DEFAULT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.supportattachments ALTER COLUMN attachmentid SET DEFAULT nextval('public.supportattachments_attachmentid_seq'::regclass);


--
-- Name: userotps otpid; Type: DEFAULT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.userotps ALTER COLUMN otpid SET DEFAULT nextval('public.userotps_otpid_seq'::regclass);


--
-- Data for Name: budgets; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.budgets (budgetid, userid, categoryid, limitamount, spentamount, periodtype, periodyear, periodmonth, periodweek, startdate, enddate, createdat, updatedat) FROM stdin;
BUD26040001	U2604150004	CAT260415002	10000000.00	500000.00	month	2026	4	\N	2026-04-01	2026-04-30	2026-04-15 14:39:36.261077	2026-04-15 21:43:14.498613
BUD26040002	U2604150004	CAT260415002	1000000.00	500000.00	week	2026	\N	16	2026-04-13	2026-04-19	2026-04-15 14:40:50.629924	2026-04-15 21:43:14.498626
BUD26040003	U2604140002	CAT260414001	500000.00	0.00	week	2026	\N	17	2026-04-20	2026-04-26	2026-04-16 14:54:09.787046	2026-04-16 14:54:09.787046
BUD26040004	U2604140002	CAT260414001	2000000.00	0.00	month	2026	5	\N	2026-05-01	2026-05-31	2026-04-16 14:54:28.323957	2026-04-16 14:54:28.323957
BUD26040007	U2604150001	CAT260414001	3000000.00	30000.00	month	2026	4	\N	2026-04-01	2026-04-30	2026-04-17 08:42:40.203745	2026-04-17 08:42:40.203745
BDG2604170001	U2604150002	CAT260417003	4000.00	0.00	month	2026	4	\N	2026-04-01	2026-04-30	2026-04-17 16:14:28.984931	2026-04-17 23:16:35.303577
BDG2604170002	U2604150001	CAT260417005	1500000.00	2000000.00	week	2026	\N	16	2026-04-13	2026-04-19	2026-04-17 16:39:43.815741	2026-04-17 23:40:31.523085
BDG2604170003	U2604150001	CAT000000007	1000000.00	500000.00	month	2026	4	\N	2026-04-01	2026-04-30	2026-04-17 16:47:29.944126	2026-04-17 16:47:29.944126
BUD26040005	U2604150001	CAT260417001	2000000.00	150000.00	month	2026	4	\N	2026-04-01	2026-04-30	2026-04-17 08:32:17.500767	2026-04-18 01:15:18.993826
\.


--
-- Data for Name: categories; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.categories (categoryid, userid, categoryname, icon, color, isdefault, isdeleted, createdat, updatedat, categorytype) FROM stdin;
CAT260415002	U2604150004	Giao tiếp xã hội	/sneat/img/icons/categories/donate.svg	#71dd37	f	f	2026-04-15 14:37:32.676795	2026-04-15 21:42:04.354469	expense
CAT260414001	\N	Ăn uống	/sneat/img/icons/categories/hamburger.svg	#D85A30	t	f	2026-04-14 00:01:30.641861	2026-04-14 00:01:30.641861	expense
CAT260414002	\N	Đi lại	/sneat/img/icons/categories/car-side.svg	#185FA5	t	f	2026-04-14 00:01:30.641861	2026-04-14 00:01:30.641861	expense
CAT260414003	\N	Mua sắm	/sneat/img/icons/categories/shopping-cart.svg	#993556	t	f	2026-04-14 00:01:30.641861	2026-04-14 00:01:30.641861	expense
CAT000000004	\N	Giải trí	/sneat/img/icons/categories/film.svg	#534AB7	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT000000005	\N	Sức khỏe	/sneat/img/icons/categories/medicine.svg	#0F6E56	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT000000006	\N	Giáo dục	/sneat/img/icons/categories/graduation-cap.svg	#854F0B	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT000000007	\N	Hóa đơn	/sneat/img/icons/categories/file-invoice-dollar.svg	#5F5E5A	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT000000008	\N	Tiết kiệm	/sneat/img/icons/categories/sack-dollar.svg	#3B6D11	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT000000009	\N	Du lịch	/sneat/img/icons/categories/plane.svg	#1D9E75	t	f	2026-04-15 10:27:59.8767	2026-04-15 10:27:59.8767	expense
CAT260414010	\N	Khác	/sneat/img/icons/categories/question-square.svg	#888780	t	f	2026-04-14 00:01:30.641861	2026-04-14 00:01:30.641861	expense
CAT260416002	U2604140002	Hoàng gay	/sneat/img/icons/categories/chess-knight-alt.svg	#23a993	f	t	2026-04-16 14:55:55.99161	2026-04-16 23:41:22.342411	expense
CAT260417001	U2604150001	Thú cưng	/sneat/img/icons/categories/paw.svg	#ea62b8	f	f	2026-04-17 08:31:51.660595	2026-04-17 15:39:51.818773	expense
CAT260417002	U2604150001	Từ thiện	/sneat/img/icons/categories/donate.svg	#71dd37	f	t	2026-04-17 08:38:46.5502	2026-04-17 15:45:25.833538	expense
CAT260416001	U2604140002	Lương	/sneat/img/icons/categories/sack-dollar.svg	#243f24	f	f	2026-04-16 14:53:38.58836	2026-04-16 14:53:38.58836	income
CAT260417004	U2604150001	Buôn bán	/sneat/img/icons/categories/dollar.svg	#3084df	f	f	2026-04-17 14:23:18.187622	2026-04-17 21:23:55.533549	income
CAT260415001	U2604150002	Test	/sneat/img/icons/categories/pizza-slice.svg	#ffab00	f	t	2026-04-15 14:36:49.174903	2026-04-17 23:13:53.32687	expense
CAT260417003	U2604150002	Test2	/sneat/img/icons/categories/mug-hot-alt.svg	#ffab00	f	f	2026-04-17 13:14:23.440591	2026-04-17 23:16:36.870337	expense
CAT260417005	U2604150001	Karaoke	/sneat/img/icons/categories/glass-cheers.svg	#71dd37	f	f	2026-04-17 16:38:03.480653	2026-04-17 23:39:55.154269	expense
\.


--
-- Data for Name: supportattachments; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.supportattachments (attachmentid, supportrequestid, filename, fileurl, filetype, filesize, createdat) FROM stdin;
1	SR260415000004	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/5872bbf765b74d2f90040d4a61dfbea8.jpg	image/jpeg	23723	2026-04-15 07:17:00.681111
2	SR260415000005	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/9627417cceb8472f97c8bebe9fcf6e1a.jpg	image/jpeg	23723	2026-04-15 07:17:03.569055
3	SR260415000006	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/5d42bad0a0744026bc271811a40731f2.jpg	image/jpeg	23723	2026-04-15 07:17:05.27811
4	SR260415000007	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/d00f4cc0a973451fb80ad58e23002480.jpg	image/jpeg	23723	2026-04-15 07:17:09.899935
5	SR260415000008	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/bb5360c600e94ac2b491eeed4f76f281.jpg	image/jpeg	23723	2026-04-15 07:17:11.476225
6	SR260415000009	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/c52b7fd8d37949d3b1ec0887e17a63fd.jpg	image/jpeg	23723	2026-04-15 07:17:13.273702
7	SR260415000010	4e82420d6dbfdf4962dd8e4993b7fbf4.jpg	/uploads/support/2026/04/464fcebc650b4f4fbe9a5a943416c78e.jpg	image/jpeg	23723	2026-04-15 07:17:15.477711
8	SR260415000011	DetailedClassDiagram.png	/uploads/support/2026/04/43b0fda1d9754fc78178df1886585b3b.png	image/png	51432	2026-04-15 09:08:35.539735
9	SR260415000012	caibap.jpg	/uploads/support/2026/04/f7fad6992a7549dba5b4a6954c6953d1.jpg	image/jpeg	82323	2026-04-15 14:45:34.489114
10	SR260415000013	canhcut.jpg	/uploads/support/2026/04/fe1ee92bc6f04aed9330fb3c20089c96.jpg	image/jpeg	57516	2026-04-15 14:54:31.3164
11	SR260415000014	dog.jpg	/uploads/support/2026/04/e600a93b8ce74f24896e8114a170e8c6.jpg	image/jpeg	84358	2026-04-15 15:22:26.088628
12	SR260415000015	tom.jpg	/uploads/support/2026/04/44d55f601eac4b1a9024be8b79ef3b81.jpg	image/jpeg	9565	2026-04-15 15:25:02.315516
13	SR260416000001	Brian Griffin Pfp _ Icon.jpg	/uploads/support/2026/04/62197edd69bf48d78cb91fa141e3d6f8.jpg	image/jpeg	47037	2026-04-16 15:27:46.033922
14	SR260416000002	tom.jpg	/uploads/support/2026/04/dcb7e10a20f84d4794ab839813b4d34a.jpg	image/jpeg	9565	2026-04-16 15:50:26.150549
15	SR260416000003	Cool Quag.jpg	/uploads/support/2026/04/9006cc7c5f034dfc8c96e1c8ec23627e.jpg	image/jpeg	27366	2026-04-16 16:24:50.887316
16	SR260416000004	tomcall.mp4	/uploads/support/2026/04/d0c9e5568603403b81d83e669b7b5e1d.mp4	video/mp4	2866933	2026-04-16 16:38:55.180964
17	SR260417000001	FB_IMG_1756281673122.jpg	/uploads/support/2026/04/03193882817343d88cc954b67e566124.jpg	image/jpeg	158032	2026-04-17 08:47:20.643045
18	SR260417000002	Screenshot (1150).png	/uploads/support/2026/04/d74aff14e555427d9bbb003038fa766c.png	image/png	628887	2026-04-17 14:47:06.288006
19	SR260417000003	download.webp	/uploads/support/2026/04/c8bdbe0c8e844b5783395960920fae07.webp	image/webp	37120	2026-04-17 15:29:35.828001
20	SR260417000004	Tester-là-gì-4.jpg	/uploads/support/2026/04/0587a5d28c324544ad858f2ff98eba1c.jpg	image/jpeg	32268	2026-04-17 15:32:30.554474
21	SR260417000005	AK đ gì đây.jpeg	/uploads/support/2026/04/428a779d9e2e43e48e43bb3442c04a1e.jpeg	image/jpeg	28349	2026-04-17 17:30:47.064946
22	SR260417000006	Peter.webp	/uploads/support/2026/04/46f8bc50cbb54e3e844bbbdc0b9aeae8.webp	image/webp	43598	2026-04-17 17:38:45.518594
\.


--
-- Data for Name: supportrequests; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.supportrequests (supportrequestid, userid, subject, message, supporttype, priority, status, adminreply, createdat, updatedat, viewedat, repliedat, closedat) FROM stdin;
SR260415000001	U2604150001	thu nghiem	hghghghg	bug	medium	pending	\N	2026-04-15 04:15:51.178197	2026-04-15 04:15:51.178197	\N	\N	\N
SR260415000002	U2604150001	thu nghiem	hghghghg	bug	medium	pending	\N	2026-04-15 04:15:58.797183	2026-04-15 04:15:58.797183	\N	\N	\N
SR260415000003	U2604150001	thu nghiem	hghghghg	bug	medium	replied	\N	2026-04-15 04:20:22.46739	2026-04-15 13:14:12.004299	2026-04-15 13:14:12.004299	2026-04-15 13:14:12.004299	\N
SR260415000004	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	pending	\N	2026-04-15 07:16:59.889521	2026-04-15 07:16:59.889521	\N	\N	\N
SR260415000005	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	pending	\N	2026-04-15 07:17:02.017273	2026-04-15 07:17:02.017273	\N	\N	\N
SR260415000006	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	pending	\N	2026-04-15 07:17:04.586615	2026-04-15 07:17:04.586615	\N	\N	\N
SR260415000008	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	pending	\N	2026-04-15 07:17:10.259246	2026-04-15 07:17:10.259246	\N	\N	\N
SR260415000009	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	pending	\N	2026-04-15 07:17:12.034156	2026-04-15 07:17:12.034156	\N	\N	\N
SR260415000011	U2604140002	App chán quá shop ơi	App chán qua shop ơi	bug	medium	pending	\N	2026-04-15 09:08:35.139517	2026-04-15 09:08:35.139517	\N	\N	\N
SR260417000001	U2604150001	Không thể đổi avatar	tôi không thể đổi avatar cho tài khoản của mình	bug	high	closed	bình tĩnh nào bro	2026-04-17 08:47:20.375263	2026-04-17 20:52:39.813532	2026-04-17 16:27:45.010947	2026-04-17 20:52:39.813532	2026-04-17 20:52:39.813532
SR260415000015	U2604150001	giao dịch lỗi	không thể thực hiện giao dịch	transaction	high	closed	hãy kiên nhẫn chờ đợi bạn nhé!	2026-04-15 15:25:01.772683	2026-04-17 20:53:03.960325	2026-04-15 22:26:23.490642	2026-04-17 20:53:03.960325	2026-04-17 20:53:03.960325
SR260417000002	U2604150001	Vu vơ	Không có gì hết đâu	other	low	pending	\N	2026-04-17 14:47:06.002407	2026-04-17 14:47:06.002407	\N	\N	\N
SR260417000003	U2604150001	Thêm tính năng đi	Cập nhật tính năng mới đi admin	feature	high	pending	\N	2026-04-17 15:29:35.532901	2026-04-17 15:29:35.532901	\N	\N	\N
SR260415000013	U2604150001	Lỗi rồi	app load lâu thế	bug	high	replied	\N	2026-04-15 14:54:31.020604	2026-04-15 21:58:20.261178	2026-04-15 21:55:45.465673	2026-04-15 21:58:20.261178	\N
SR260415000014	U2604150001	helppppppp	tài khoản của tôi bị hack	account	high	replied	bỏ đi bạn ơi	2026-04-15 15:22:25.644582	2026-04-15 22:23:25.455204	2026-04-15 22:23:25.455204	2026-04-15 22:23:25.455204	\N
SR260415000012	U2604150004	Không thể lưu giao dịch chi tiêu	Tôi thực hiện giao dịch nhưng hệ thống không ghi lại lịch sử	bug	high	replied	kệ nó đi bạn	2026-04-15 14:45:34.148888	2026-04-15 22:28:27.122938	2026-04-15 21:49:41.205388	2026-04-15 22:28:27.122938	\N
SR260415000007	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	closed	\N	2026-04-15 07:17:07.409782	2026-04-16 22:11:01.721711	2026-04-16 22:11:01.721711	\N	2026-04-16 22:11:01.721711
SR260416000001	U2604150004	Tính năng	Thêm tính năng mới đi bạn	feature	medium	replied	ok bạn ơi, hãy chờ nhé	2026-04-16 15:27:45.681744	2026-04-16 22:29:30.057367	2026-04-16 22:29:30.057367	2026-04-16 22:29:30.057367	\N
SR260416000002	U2604150004	Help me	Tài khoản của tôi bị hạn chế tính năng	account	low	closed	xóa luôn đi bạn	2026-04-16 15:50:25.868714	2026-04-16 23:19:51.010396	2026-04-16 22:52:05.462126	2026-04-16 23:19:51.010396	2026-04-16 23:19:51.010396
SR260416000003	U2604150001	Hello World	không có gì	bug	high	replied	đẹp zai đấy	2026-04-16 16:24:50.616083	2026-04-16 23:26:15.087442	2026-04-16 23:26:15.087442	2026-04-16 23:26:15.087442	\N
SR260417000004	U2604150003	Đánh giá Web App Quản lý Chi tiêu	App không cho miễn phí tiền khi vào, sì cam, Admin hãy cho tôi xin ít xiền đi	feature	urgent	closed	Mơ à, nạp tiền đi cu	2026-04-17 15:32:30.224186	2026-04-18 00:20:47.267428	2026-04-17 22:34:00.196512	2026-04-18 00:20:47.267428	2026-04-18 00:20:47.267428
SR260417000005	U2604150003	App chán quá shop ơi	Hoàng qá béo	feature	urgent	pending	\N	2026-04-17 17:30:46.743821	2026-04-17 17:30:46.743821	\N	\N	\N
SR260416000004	U2604150001	Video	xem video này đi	feature	high	closed	hay đấy	2026-04-16 16:38:54.89991	2026-04-16 23:42:04.890482	2026-04-16 23:40:54.949631	2026-04-16 23:42:04.890482	2026-04-16 23:42:04.890482
SR260415000010	U2604150002	Web như ...	tôi thấy giao diện quá xấu	other	urgent	replied	Con mẹ m béo	2026-04-15 07:17:14.382627	2026-04-16 23:46:46.985596	2026-04-16 23:46:46.985596	2026-04-16 23:46:46.985596	\N
SR260417000006	U2604150001	ssđ	fghghhjjythg	bug	medium	pending	\N	2026-04-17 17:38:45.22715	2026-04-17 17:38:45.22715	\N	\N	\N
\.


--
-- Data for Name: transactions; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.transactions (transactionid, userid, walletid, categoryid, transactiontype, amount, transactiondate, note, isrecurring, recurinterval, createdat, updatedat) FROM stdin;
TXN2604150001	U2604150004	W00040001	CAT260415002	expense	500000.00	2026-04-15	Đi đám cưới bạn thân	f	\N	2026-04-15 14:43:04.982372	2026-04-15 14:43:04.982372
TXN2604160001	U2604150002	W00020002	CAT260414003	expense	2000.00	2026-04-16	\N	f	\N	2026-04-16 14:27:15.162815	2026-04-16 14:27:15.162815
TXN2604160002	U2604150002	W00020002	CAT260414002	expense	40000.00	2026-04-16	\N	f	\N	2026-04-16 14:27:27.420954	2026-04-16 14:27:27.420954
TXN2604160003	U2604150002	W00020002	CAT260414001	expense	20000.00	2026-04-16	\N	f	\N	2026-04-16 14:27:49.821421	2026-04-16 14:27:49.821421
TXN2604160004	U2604140002	W00020001	CAT260414001	expense	36000.00	2026-04-16	Bún đậu 36 Pháo Đài Láng	f	\N	2026-04-16 15:07:28.084619	2026-04-16 22:07:50.689581
TXN2604160005	U2604150004	W00040001	CAT000000005	expense	100000.00	2026-04-15	mua thuốc cảm cúm	f	\N	2026-04-16 15:24:36.352625	2026-04-16 22:25:08.614469
TXN2604170002	U2604150001	W00010002	CAT260414001	expense	30000.00	2026-04-17	Mua bánh mì	f	\N	2026-04-17 08:41:26.040345	2026-04-17 08:41:26.040345
TXN2604170003	U2604150001	W00010002	CAT260417001	expense	50000.00	2026-04-17	Mua tăm ủng hộ người mù	f	\N	2026-04-17 08:43:46.595335	2026-04-17 15:45:25.719949
TXN2604170004	U2604140002	W00020001	CAT260414001	expense	2000000.00	2026-04-16	Chuyển đến [Ăn uống]: Tiền ăn tháng 4/26	f	\N	2026-04-17 09:17:17.079407	2026-04-17 09:17:17.079407
TXN2604170005	U2604140002	W00020003	CAT260414001	income	2000000.00	2026-04-16	Nhận từ [Tài khoản chính]: Tiền ăn tháng 4/26	f	\N	2026-04-17 09:17:17.079407	2026-04-17 09:17:17.079407
TXN2604170006	U2604140002	W00020001	CAT260414001	expense	36000.00	2026-04-16	Chuyển đến [Ăn uống]: Mua nem chua	f	\N	2026-04-17 09:18:23.878137	2026-04-17 09:18:23.878137
TXN2604170007	U2604140002	W00020003	CAT260414001	income	36000.00	2026-04-16	Nhận từ [Tài khoản chính]: Mua nem chua	f	\N	2026-04-17 09:18:23.878137	2026-04-17 09:18:23.878137
TXN2604170008	U2604150001	W00010003	CAT000000007	expense	500000.00	2026-04-17	Đóng tiền điện tháng 4	f	\N	2026-04-17 14:21:21.290543	2026-04-17 14:21:21.290543
TXN2604170009	U2604150001	W00010002	CAT260417004	income	200000.00	2026-04-17	Bán chó	f	\N	2026-04-17 14:25:01.047349	2026-04-17 14:25:01.047349
TXN2604170010	U2604150001	W00010003	CAT000000006	expense	100000.00	2026-04-17	Mua sách vở	f	\N	2026-04-17 14:25:58.110777	2026-04-17 14:25:58.110777
TXN2604170011	U2604150001	W00010003	CAT260417004	income	50000.00	2026-04-17	Bán gà	f	\N	2026-04-17 14:26:32.106544	2026-04-17 14:26:32.106544
TXN2604170012	U2604150001	W00010003	CAT260417005	expense	2000000.00	2026-04-17	Đi hát	f	\N	2026-04-17 16:40:23.016055	2026-04-17 16:40:23.016055
TXN2604180001	U2604150001	W00010003	CAT260417001	expense	100000.00	2026-04-17	mua cơm cho chó	f	\N	2026-04-17 18:15:10.460301	2026-04-17 18:15:10.460301
\.


--
-- Data for Name: userotps; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.userotps (otpid, email, otpcode, isused, expiresat, createdat) FROM stdin;
1	luutunglam3175@gmail.com	371856	t	2026-04-13 17:40:54.878966	2026-04-13 17:35:59.067349
2	luonghoaian2005@gmail.com	641399	t	2026-04-15 03:18:25.878214	2026-04-15 03:13:16.86603
3	luonghoaian2005@gmail.com	034988	t	2026-04-15 03:19:32.665807	2026-04-15 03:14:23.63611
4	hoangnguyen72.personal@gmail.com	615330	t	2026-04-15 06:21:23.71124	2026-04-15 06:16:24.005385
5	noobergg80@gmail.com	525274	t	2026-04-15 07:11:33.302743	2026-04-15 07:06:33.400485
6	tieutieu0204@gmail.com	031681	t	2026-04-15 14:33:41.046148	2026-04-15 14:28:32.09227
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.users (userid, fullname, email, passwordhash, phonenumber, avatar, role, status, createdat, updatedat) FROM stdin;
U2604140001	Quản trị viên	admin@expenseapp.vn	hashed_password_sample	\N	\N	admin	active	2026-04-14 00:01:30.699893	2026-04-14 00:01:30.699893
U2604150002	Hoàng Nguyễn	hoangnguyen72.personal@gmail.com	$pbkdf2-sha256$29000$vfe.V8rZu9f6v1cKofQeYw$WoOM9.uSSbS9nDGice8te/LILcKT4mUCMlJMXnikfU0	\N	\N	user	active	2026-04-15 06:16:23.443884	2026-04-15 06:17:01.888917
U2604150004	tester	tieutieu0204@gmail.com	$pbkdf2-sha256$29000$GUNIScn5v5fyXovxvneOUQ$YOvgQ.n9wOZncDPfN0MZDPhjC4xoCG1og9Rqae0w5no	\N	/sneat/img/avatars/default/teams_1.png	user	active	2026-04-15 14:28:31.343891	2026-04-15 14:28:55.402281
UADMIN000001	MIMO_ADMIN	lambruhak2l@gmail.com	$pbkdf2-sha256$29000$1xqDEALAWMtZ673XGqM0pg$LA73NUVWogEmOthiV22S5Uq9bWMTGQIEn7Ookw5PEYU	0366636666	/sneat/img/avatars/upload/lambruhak2l@gmail.com.png	admin	active	2026-04-15 11:24:14.454059	2026-04-17 02:56:19.246963
U2604140002	Lưu Tùng Lâm	luutunglam3175@gmail.com	$pbkdf2-sha256$29000$lHLu/V.rdQ6hlPL.f6.V8g$Idmi/cL4QM7CJaMJjNpAqNdbs6dDJxLPgYO5.YeJgSY	0366162554	/sneat/img/avatars/upload/luutunglam3175@gmail.com.png	user	active	2026-04-13 17:35:58.575499	2026-04-17 03:06:30.193964
U2604150001	quác mai ơ	luonghoaian2005@gmail.com	$pbkdf2-sha256$29000$vNd6TwnBmNN6793731sLwQ$GCw50E8XCP046VsUxeb52UR7SR1quvyf9HrUnuhqOFM	0999363181	/sneat/img/avatars/upload/luonghoaian2005@gmail.com.png	user	active	2026-04-15 03:13:12.17592	2026-04-18 00:07:09.356729
U2604150003	Mai Béo	noobergg80@gmail.com	$pbkdf2-sha256$29000$pxQiZKz13jvHGAMAQEgJ4Q$CWGvXYlyuCXzX0lgCwTfqLm4pmyuvyWARxzTx7mwxk4	0366636366	/sneat/img/avatars/upload/noobergg80@gmail.com.png	user	active	2026-04-15 07:06:29.106537	2026-04-18 00:29:02.285661
\.


--
-- Data for Name: wallets; Type: TABLE DATA; Schema: public; Owner: mimodb
--

COPY public.wallets (walletid, userid, walletname, initialbalance, currentbalance, currency, isdefault, createdat, updatedat) FROM stdin;
W00010001	U2604140001	Tiền mặt	5000000.00	5000000.00	VND	t	2026-04-14 00:01:30.754891	2026-04-14 00:01:30.754891
W26041500006	U2604150003	Tài khoản chính	0.00	0.00	VND	t	2026-04-15 07:06:29.106537	2026-04-15 07:06:29.106537
W26041500007	U2604150004	Tài khoản chính	0.00	0.00	VND	f	2026-04-15 14:28:31.343891	2026-04-15 14:31:33.651417
W00020002	U2604150002	Tiền	300000.00	238000.00	VND	t	2026-04-16 14:26:35.013342	2026-04-16 21:27:49.864186
W00040001	U2604150004	Cát Tường	50000000.00	49400000.00	VND	t	2026-04-15 14:31:34.094406	2026-04-16 22:25:08.614498
W00020001	U2604140002	Tài khoản chính	5000000.00	2928000.00	VND	t	2026-04-15 06:16:29.626047	2026-04-17 16:18:22.63304
W00020003	U2604140002	Ăn uống	0.00	2036000.00	VND	f	2026-04-16 14:58:44.414171	2026-04-17 16:18:22.633045
W00010002	U2604150001	Phú Quý	100000.00	220000.00	VND	f	2026-04-17 08:34:34.859117	2026-04-17 21:25:09.556117
W00010003	U2604150001	Cát Tường	50000000.00	47350000.00	VND	f	2026-04-17 14:20:25.95363	2026-04-18 01:15:18.994064
\.


--
-- Name: seq_walletid; Type: SEQUENCE SET; Schema: public; Owner: mimodb
--

SELECT pg_catalog.setval('public.seq_walletid', 7, true);


--
-- Name: supportattachments_attachmentid_seq; Type: SEQUENCE SET; Schema: public; Owner: mimodb
--

SELECT pg_catalog.setval('public.supportattachments_attachmentid_seq', 22, true);


--
-- Name: userotps_otpid_seq; Type: SEQUENCE SET; Schema: public; Owner: mimodb
--

SELECT pg_catalog.setval('public.userotps_otpid_seq', 6, true);


--
-- Name: budgets budgets_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_pkey PRIMARY KEY (budgetid);


--
-- Name: categories categories_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_pkey PRIMARY KEY (categoryid);


--
-- Name: categories categories_userid_categoryname_key; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_userid_categoryname_key UNIQUE (userid, categoryname);


--
-- Name: supportattachments supportattachments_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.supportattachments
    ADD CONSTRAINT supportattachments_pkey PRIMARY KEY (attachmentid);


--
-- Name: supportrequests supportrequests_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.supportrequests
    ADD CONSTRAINT supportrequests_pkey PRIMARY KEY (supportrequestid);


--
-- Name: transactions transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_pkey PRIMARY KEY (transactionid);


--
-- Name: userotps userotps_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.userotps
    ADD CONSTRAINT userotps_pkey PRIMARY KEY (otpid);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (userid);


--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (walletid);


--
-- Name: wallets wallets_userid_walletname_key; Type: CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_userid_walletname_key UNIQUE (userid, walletname);


--
-- Name: idx_supportrequests_user_id; Type: INDEX; Schema: public; Owner: mimodb
--

CREATE INDEX idx_supportrequests_user_id ON public.supportrequests USING btree (userid);


--
-- Name: ix_supportrequests_supportrequestid; Type: INDEX; Schema: public; Owner: mimodb
--

CREATE INDEX ix_supportrequests_supportrequestid ON public.supportrequests USING btree (supportrequestid);


--
-- Name: ix_supportrequests_userid; Type: INDEX; Schema: public; Owner: mimodb
--

CREATE INDEX ix_supportrequests_userid ON public.supportrequests USING btree (userid);


--
-- Name: ix_transactions_user_date; Type: INDEX; Schema: public; Owner: mimodb
--

CREATE INDEX ix_transactions_user_date ON public.transactions USING btree (userid, transactiondate DESC);


--
-- Name: ux_budgets_usercatperiod; Type: INDEX; Schema: public; Owner: mimodb
--

CREATE UNIQUE INDEX ux_budgets_usercatperiod ON public.budgets USING btree (userid, categoryid, periodtype, periodyear, periodmonth_nonull, periodweek_nonull);


--
-- Name: users trg_users_createdefaultwallet; Type: TRIGGER; Schema: public; Owner: mimodb
--

CREATE TRIGGER trg_users_createdefaultwallet AFTER INSERT ON public.users FOR EACH ROW EXECUTE FUNCTION public.fn_createdefaultwallet();


--
-- Name: users trg_users_setdefaultavatar; Type: TRIGGER; Schema: public; Owner: mimodb
--

CREATE TRIGGER trg_users_setdefaultavatar BEFORE INSERT ON public.users FOR EACH ROW EXECUTE FUNCTION public.fn_setdefaultavatar();


--
-- Name: budgets budgets_categoryid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_categoryid_fkey FOREIGN KEY (categoryid) REFERENCES public.categories(categoryid);


--
-- Name: budgets budgets_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.budgets
    ADD CONSTRAINT budgets_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid) ON DELETE CASCADE;


--
-- Name: categories categories_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.categories
    ADD CONSTRAINT categories_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid) ON DELETE SET NULL;


--
-- Name: supportattachments supportattachments_supportrequestid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.supportattachments
    ADD CONSTRAINT supportattachments_supportrequestid_fkey FOREIGN KEY (supportrequestid) REFERENCES public.supportrequests(supportrequestid) ON DELETE CASCADE;


--
-- Name: supportrequests supportrequests_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.supportrequests
    ADD CONSTRAINT supportrequests_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid) ON DELETE CASCADE;


--
-- Name: transactions transactions_categoryid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_categoryid_fkey FOREIGN KEY (categoryid) REFERENCES public.categories(categoryid);


--
-- Name: transactions transactions_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid) ON DELETE CASCADE;


--
-- Name: transactions transactions_walletid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.transactions
    ADD CONSTRAINT transactions_walletid_fkey FOREIGN KEY (walletid) REFERENCES public.wallets(walletid);


--
-- Name: wallets wallets_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mimodb
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_userid_fkey FOREIGN KEY (userid) REFERENCES public.users(userid) ON DELETE CASCADE;


--
-- Name: DEFAULT PRIVILEGES FOR SEQUENCES; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON SEQUENCES TO mimodb;


--
-- Name: DEFAULT PRIVILEGES FOR TYPES; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TYPES TO mimodb;


--
-- Name: DEFAULT PRIVILEGES FOR FUNCTIONS; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON FUNCTIONS TO mimodb;


--
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES TO mimodb;


--
-- PostgreSQL database dump complete
--

\unrestrict iVia5K5cVEIsomKLcOwbSR5jCieJ0N4k5BLG5nUmgv5nCeZs0o5bHFH1jXsqdiC

