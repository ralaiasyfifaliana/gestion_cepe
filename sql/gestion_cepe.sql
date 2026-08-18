--
-- PostgreSQL database dump
--

\restrict AQyzU1AneZg8rwxsqITKFZJCWxO3XCmwpBd0jF9vsVg5wzwD3GagiqJItXWDcce

-- Dumped from database version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)
-- Dumped by pg_dump version 16.14 (Ubuntu 16.14-0ubuntu0.24.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE IF EXISTS "CEPE";
--
-- Name: CEPE; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "CEPE" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'fr_FR.UTF-8';


ALTER DATABASE "CEPE" OWNER TO postgres;

\unrestrict AQyzU1AneZg8rwxsqITKFZJCWxO3XCmwpBd0jF9vsVg5wzwD3GagiqJItXWDcce
\connect "CEPE"
\restrict AQyzU1AneZg8rwxsqITKFZJCWxO3XCmwpBd0jF9vsVg5wzwD3GagiqJItXWDcce

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
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
-- Name: ecole; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ecole (
    numecole character varying(20) NOT NULL,
    design character varying(150) NOT NULL,
    adresse character varying(200)
);


ALTER TABLE public.ecole OWNER TO postgres;

--
-- Name: eleve; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.eleve (
    numeleve character varying(20) NOT NULL,
    numecole character varying(20) NOT NULL,
    nom character varying(100) NOT NULL,
    prenom character varying(100) NOT NULL
);


ALTER TABLE public.eleve OWNER TO postgres;

--
-- Name: matiere; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.matiere (
    nummat character varying(20) NOT NULL,
    designmat character varying(100) NOT NULL,
    coef integer NOT NULL,
    CONSTRAINT matiere_coef_check CHECK ((coef > 0))
);


ALTER TABLE public.matiere OWNER TO postgres;

--
-- Name: note; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.note (
    anneescolaire character varying(9) NOT NULL,
    numeleve character varying(20) NOT NULL,
    nummat character varying(20) NOT NULL,
    note double precision NOT NULL,
    CONSTRAINT note_note_check CHECK (((note >= (0)::double precision) AND (note <= (20)::double precision)))
);


ALTER TABLE public.note OWNER TO postgres;

--
-- Data for Name: ecole; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ecole (numecole, design, adresse) FROM stdin;
ECO002	Saint Joseph d'Aost	Antananarivo
ECO001	Saint Joseph Ouvrier	Antananarivo
ECO003	Note Dame	Antananarivo
ECO004	Ny Sekolintsika	Antananarivo
\.


--
-- Data for Name: eleve; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.eleve (numeleve, numecole, nom, prenom) FROM stdin;
ELV001	ECO001	RAKOTO	Bernard
ELV002	ECO002	RANDRIA	Jean
ELV003	ECO003	LUC	Fitiavana
ELV005	ECO004	Bertrand	Randria
\.


--
-- Data for Name: matiere; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.matiere (nummat, designmat, coef) FROM stdin;
MAT01	Malagasy	3
MAT02	Calcul	1
MAT03	Probleme	2
MAT04	Tantara	1
MAT05	Geographie	1
MAT06	Francais	1
MAT07	SVT	2
\.


--
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.note (anneescolaire, numeleve, nummat, note) FROM stdin;
2022-2023	ELV001	MAT02	19
2022-2023	ELV001	MAT06	15
2022-2023	ELV001	MAT05	14
2022-2023	ELV001	MAT01	12
2022-2023	ELV001	MAT03	19
2022-2023	ELV001	MAT07	9
2022-2023	ELV001	MAT04	11
2022-2023	ELV002	MAT02	9.75
2022-2023	ELV002	MAT06	9.75
2022-2023	ELV002	MAT05	9.75
2022-2023	ELV002	MAT01	9.75
2022-2023	ELV002	MAT03	9.75
2022-2023	ELV002	MAT07	9.75
2022-2023	ELV002	MAT04	9.75
2022-2023	ELV003	MAT02	8
2022-2023	ELV003	MAT06	8
2022-2023	ELV003	MAT05	10
2022-2023	ELV003	MAT01	6.5
2022-2023	ELV003	MAT03	15
2022-2023	ELV003	MAT07	12
2022-2023	ELV003	MAT04	2
2022-2023	ELV005	MAT02	9.75
2022-2023	ELV005	MAT06	9.75
2022-2023	ELV005	MAT05	9.75
2022-2023	ELV005	MAT01	9.75
2022-2023	ELV005	MAT03	9.75
2022-2023	ELV005	MAT07	9.75
2022-2023	ELV005	MAT04	9.75
\.


--
-- Name: ecole pk_ecole; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ecole
    ADD CONSTRAINT pk_ecole PRIMARY KEY (numecole);


--
-- Name: eleve pk_eleve; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eleve
    ADD CONSTRAINT pk_eleve PRIMARY KEY (numeleve);


--
-- Name: matiere pk_matiere; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.matiere
    ADD CONSTRAINT pk_matiere PRIMARY KEY (nummat);


--
-- Name: note pk_note; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT pk_note PRIMARY KEY (anneescolaire, numeleve, nummat);


--
-- Name: eleve fk_eleve_ecole; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.eleve
    ADD CONSTRAINT fk_eleve_ecole FOREIGN KEY (numecole) REFERENCES public.ecole(numecole) ON UPDATE CASCADE ON DELETE RESTRICT;


--
-- Name: note fk_note_eleve; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT fk_note_eleve FOREIGN KEY (numeleve) REFERENCES public.eleve(numeleve) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: note fk_note_matiere; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT fk_note_matiere FOREIGN KEY (nummat) REFERENCES public.matiere(nummat) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict AQyzU1AneZg8rwxsqITKFZJCWxO3XCmwpBd0jF9vsVg5wzwD3GagiqJItXWDcce

