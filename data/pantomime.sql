-- MySQL dump 10.13  Distrib 5.1.67, for apple-darwin12.2.0 (i386)
--
-- Host: localhost    Database: pantomime
-- ------------------------------------------------------
-- Server version	5.1.67

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `mail`
--

DROP TABLE IF EXISTS `mail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `mail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mail`
--

LOCK TABLES `mail` WRITE;
/*!40000 ALTER TABLE `mail` DISABLE KEYS */;
INSERT INTO `mail` VALUES (3,'From barbee@oliveoil.com Fri Jul 26 08:26:05 2013\nReturn-Path: <barbee@oliveoil.com>\nReceived: from smtp.darkfog.org ([10.20.10.20])\n	 by imap.darkfog.org (Cyrus v2.3.14) with LMTPA;\n	 Fri, 26 Jul 2013 12:26:05 +0000\nX-Sieve: CMU Sieve 2.3\nReceived: from rcdn-iport-8.oliveoil.com (rcdn-iport-8.oliveoil.com [173.37.86.79])\n	(using TLSv1 with cipher RC4-SHA (128/128 bits))\n	(No client certificate requested)\n	by smtp.darkfog.org (Postfix) with ESMTPS id 07DF4142057\n	for <barbee@darkfog.org>; Fri, 26 Jul 2013 12:25:48 +0000 (UTC)\nDKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple;\n  d=oliveoil.com; i=@oliveoil.com; l=1445; q=dns/txt; s=iport;\n  t=1374840345; x=1376049945;\n  h=from:to:subject:date:message-id:mime-version;\n  bh=5Ov/R6NMnVpyEr0Sa4zwSX2uF9Kk6FSjxJ8ziDlfhwQ=;\n  b=ZYNTC701QPgVwEchNfZRXS9xfcqidtaoJQYLx3YcwpAsWHMffKryJ4pi\n   HMTgTlfbmm+GvBKKDB3WMOEBd45LsBcB+MQplpuVBmoikb90Lb/K5EcZ1\n   1jSW2O9Te3DccbC7DQt2+BrntyJbJm6dMjUwOjbFAYW4QGkLNi3XLySvv\n   o=;\nX-Files: test.txt : 5\nX-IronPort-Anti-Spam-Filtered: true\nX-IronPort-Anti-Spam-Result: AgcFAIFl8lGtJV2d/2dsb2JhbABagkJEgQW9RIEWFnSCJgEEGXIBCwECHCYwJwQbBogCmDygWIY7iRGDTm8DkBKBLZdsgxSCKg\nX-IronPort-AV: E=Sophos;i=\"4.89,751,1367971200\"; \n   d=\"txt\'?scan\'208,217\";a=\"239835480\"\nReceived: from rcdn-core-6.oliveoil.com ([173.37.93.157])\n  by rcdn-iport-8.oliveoil.com with ESMTP; 26 Jul 2013 12:05:27 +0000\nReceived: from xhc-rcd-x01.oliveoil.com (xhc-rcd-x01.oliveoil.com [173.37.183.75])\n	by rcdn-core-6.oliveoil.com (8.14.5/8.14.5) with ESMTP id r6QC5RC2001671\n	(version=TLSv1/SSLv3 cipher=AES128-SHA bits=128 verify=FAIL)\n	for <barbee@darkfog.org>; Fri, 26 Jul 2013 12:05:27 GMT\nReceived: from xmb-aln-x14.oliveoil.com ([169.254.8.68]) by xhc-rcd-x01.oliveoil.com\n ([173.37.183.75]) with mapi id 14.02.0318.004; Fri, 26 Jul 2013 07:05:26\n -0500\nFrom: \"John Barbee (barbee)\" <barbee@oliveoil.com>\nTo: \"barbee@darkfog.org\" <barbee@darkfog.org>\nSubject: test 0800.\nThread-Topic: test 0800.\nThread-Index: AQHOifhp3H0CmVfWekasHAWr+pfWEQ==\nDate: Fri, 26 Jul 2013 12:05:26 +0000\nMessage-ID: <49D9ABAA8ACCE9438F0C33BF1529340D20C11D@xmb-aln-x14.oliveoil.com>\nAccept-Language: en-US\nContent-Language: en-US\nX-MS-Has-Attach: yes\nX-MS-TNEF-Correlator:\nuser-agent: Microsoft-MacOutlook/14.3.6.130613\nx-originating-ip: [10.98.105.149]\nContent-Type: multipart/mixed;\n	boundary=\"_004_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\"\nMIME-Version: 1.0\nStatus: RO\nContent-Length: 1404\nLines: 41\n\n--_004_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\nContent-Type: multipart/alternative;\n	boundary=\"_000_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\"\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\nContent-Type: text/plain; charset=\"us-ascii\"\nContent-Transfer-Encoding: quoted-printable\n\ntest\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\nContent-Type: text/html; charset=\"us-ascii\"\nContent-ID: <112E2DE40F1B0F40B5141B4FA112B7A3@emea.oliveoil.com>\nContent-Transfer-Encoding: quoted-printable\n\n<html>\n<head>\n<meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Dus-ascii\"=\n>\n</head>\n<body style=3D\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-lin=\ne-break: after-white-space; color: rgb(0, 0, 0); font-size: 14px; font-fami=\nly: Calibri, sans-serif; \">\n<div>test</div>\n</body>\n</html>\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_--\n\n--_004_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_\nContent-Type: text/plain; name=\"test.txt\"\nContent-Description: test.txt\nContent-Disposition: attachment; filename=\"test.txt\"; size=5;\n	creation-date=\"Fri, 26 Jul 2013 12:05:26 GMT\";\n	modification-date=\"Fri, 26 Jul 2013 12:05:26 GMT\"\nContent-ID: <90E871D8E638A2479086AB6E41436C43@emea.oliveoil.com>\nContent-Transfer-Encoding: base64\n\ndGVzdAo=\n\n--_004_49D9ABAA8ACCE9438F0C33BF1529340D20C11Dxmbalnx14oliveoilcom_--\n\n'),(4,'From barbee@oliveoil.com Sun Jul 28 18:17:04 2013\nReturn-Path: <barbee@oliveoil.com>\nReceived: from smtp.darkfog.org ([10.20.10.20])\n	 by imap.darkfog.org (Cyrus v2.3.14) with LMTPA;\n	 Sun, 28 Jul 2013 22:17:04 +0000\nX-Sieve: CMU Sieve 2.3\nReceived: from rcdn-iport-9.oliveoil.com (rcdn-iport-9.oliveoil.com [173.37.86.80])\n	(using TLSv1 with cipher RC4-SHA (128/128 bits))\n	(No client certificate requested)\n	by smtp.darkfog.org (Postfix) with ESMTPS id 4486914203A\n	for <barbee@darkfog.org>; Sun, 28 Jul 2013 22:16:46 +0000 (UTC)\nDKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple;\n  d=oliveoil.com; i=@oliveoil.com; l=985; q=dns/txt; s=iport;\n  t=1375048599; x=1376258199;\n  h=from:to:subject:date:message-id:mime-version;\n  bh=RHA3f1SPeF3Tl0X9gVAGbGdBG0ALK2S0wis7p8ExLkQ=;\n  b=XGOJyd0VXSgKAqVQDU5Q1VdBooeerF5Rvt9gFUgVFnRTGmDtubYv51Oh\n   RpPY7RDDrT/zdvcVZiwGL1z3BElGKaQZWWGCJ41vqA/3KBwDyRWJx6YF0\n   /KoaypL8WYEKH8b9DvgUd6u7QIdxF680tjw7Osgbc9d797xZKhn3j9oQg\n   o=;\nX-IronPort-Anti-Spam-Filtered: true\nX-IronPort-Anti-Spam-Result: AhUFAACT9VGtJV2Z/2dsb2JhbABbgkJEgQW9UoEXFnSCJgEEgQsBCwECHFYnBBuICJdGn12GO4kRg05vA6krgxSCKg\nX-IronPort-AV: E=Sophos;i=\"4.89,765,1367971200\"; \n   d=\"scan\'208,217\";a=\"237569749\"\nReceived: from rcdn-core-2.oliveoil.com ([173.37.93.153])\n  by rcdn-iport-9.oliveoil.com with ESMTP; 28 Jul 2013 21:56:20 +0000\nReceived: from xhc-rcd-x03.oliveoil.com (xhc-rcd-x03.oliveoil.com [173.37.183.77])\n	by rcdn-core-2.oliveoil.com (8.14.5/8.14.5) with ESMTP id r6SLuKQ2003854\n	(version=TLSv1/SSLv3 cipher=AES128-SHA bits=128 verify=FAIL)\n	for <barbee@darkfog.org>; Sun, 28 Jul 2013 21:56:20 GMT\nReceived: from xmb-aln-x14.oliveoil.com ([169.254.8.68]) by xhc-rcd-x03.oliveoil.com\n ([173.37.183.77]) with mapi id 14.02.0318.004; Sun, 28 Jul 2013 16:56:20\n -0500\nFrom: \"John Barbee (barbee)\" <barbee@oliveoil.com>\nTo: \"barbee@darkfog.org\" <barbee@darkfog.org>\nSubject: Quoted Printable Test.\nThread-Topic: Quoted Printable Test.\nThread-Index: AQHOi91Lskrx44P42EO842ODk+SE/g==\nDate: Sun, 28 Jul 2013 21:56:20 +0000\nMessage-ID: <49D9ABAA8ACCE9438F0C33BF1529340D20C691@xmb-aln-x14.oliveoil.com>\nAccept-Language: en-US\nContent-Language: en-US\nX-MS-Has-Attach:\nX-MS-TNEF-Correlator:\nuser-agent: Microsoft-MacOutlook/14.3.6.130613\nx-originating-ip: [10.98.105.149]\nContent-Type: multipart/alternative;\n	boundary=\"_000_49D9ABAA8ACCE9438F0C33BF1529340D20C691xmbalnx14oliveoilcom_\"\nMIME-Version: 1.0\nStatus: RO\nContent-Length: 959\nLines: 26\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C691xmbalnx14oliveoilcom_\nContent-Type: text/plain; charset=\"us-ascii\"\nContent-Transfer-Encoding: quoted-printable\n\nIf you believe that truth=3Dbeauty, then surely mathematics is the most bea=\nutiful branch of philosophy.\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C691xmbalnx14oliveoilcom_\nContent-Type: text/html; charset=\"us-ascii\"\nContent-ID: <539327DCDA42154BA6A30B061623BE5E@emea.oliveoil.com>\nContent-Transfer-Encoding: quoted-printable\n\n<html>\n<head>\n<meta http-equiv=3D\"Content-Type\" content=3D\"text/html; charset=3Dus-ascii\"=\n>\n</head>\n<body style=3D\"word-wrap: break-word; -webkit-nbsp-mode: space; -webkit-lin=\ne-break: after-white-space; color: rgb(0, 0, 0); font-size: 14px; font-fami=\nly: Calibri, sans-serif; \">\n<div>If you believe that truth=3Dbeauty, then surely mathematics is the mos=\nt beautiful branch of philosophy.</div>\n</body>\n</html>\n\n--_000_49D9ABAA8ACCE9438F0C33BF1529340D20C691xmbalnx14oliveoilcom_--\n\n');
/*!40000 ALTER TABLE `mail` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-08-29 10:59:03
