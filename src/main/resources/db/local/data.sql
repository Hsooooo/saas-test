INSERT INTO em_stock.member_term
    (idx, subject, content, active, required, create_date)
values
    (1, '서비스 이용약관', '<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"/><title>회원가입 약관동의</title><style>
/* cspell:disable-file */
/* webkit printing magic: print all background colors */
html {
	-webkit-print-color-adjust: exact;
}
* {
	box-sizing: border-box;
	-webkit-print-color-adjust: exact;
}

html,
body {
	margin: 0;
	padding: 0;
}
@media only screen {
	body {
		margin: 2em auto;
		max-width: 900px;
		color: rgb(55, 53, 47);
	}
}

body {
	line-height: 1.5;
	white-space: pre-wrap;
}

a,
a.visited {
	color: inherit;
	text-decoration: underline;
}

.pdf-relative-link-path {
	font-size: 80%;
	color: #444;
}

h1,
h2,
h3 {
	letter-spacing: -0.01em;
	line-height: 1.2;
	font-weight: 600;
	margin-bottom: 0;
}

.page-title {
	font-size: 2.5rem;
	font-weight: 700;
	margin-top: 0;
	margin-bottom: 0.75em;
}

h1 {
	font-size: 1.875rem;
	margin-top: 1.875rem;
}

h2 {
	font-size: 1.5rem;
	margin-top: 1.5rem;
}

h3 {
	font-size: 1.25rem;
	margin-top: 1.25rem;
}

.source {
	border: 1px solid #ddd;
	border-radius: 3px;
	padding: 1.5em;
	word-break: break-all;
}

.callout {
	border-radius: 3px;
	padding: 1rem;
}

figure {
	margin: 1.25em 0;
	page-break-inside: avoid;
}

figcaption {
	opacity: 0.5;
	font-size: 85%;
	margin-top: 0.5em;
}

mark {
	background-color: transparent;
}

.indented {
	padding-left: 1.5em;
}

hr {
	background: transparent;
	display: block;
	width: 100%;
	height: 1px;
	visibility: visible;
	border: none;
	border-bottom: 1px solid rgba(55, 53, 47, 0.09);
}

img {
	max-width: 100%;
}

@media only print {
	img {
		max-height: 100vh;
		object-fit: contain;
	}
}

@page {
	margin: 1in;
}

.collection-content {
	font-size: 0.875rem;
}

.column-list {
	display: flex;
	justify-content: space-between;
}

.column {
	padding: 0 1em;
}

.column:first-child {
	padding-left: 0;
}

.column:last-child {
	padding-right: 0;
}

.table_of_contents-item {
	display: block;
	font-size: 0.875rem;
	line-height: 1.3;
	padding: 0.125rem;
}

.table_of_contents-indent-1 {
	margin-left: 1.5rem;
}

.table_of_contents-indent-2 {
	margin-left: 3rem;
}

.table_of_contents-indent-3 {
	margin-left: 4.5rem;
}

.table_of_contents-link {
	text-decoration: none;
	opacity: 0.7;
	border-bottom: 1px solid rgba(55, 53, 47, 0.18);
}

table,
th,
td {
	border: 1px solid rgba(55, 53, 47, 0.09);
	border-collapse: collapse;
}

table {
	border-left: none;
	border-right: none;
}

th,
td {
	font-weight: normal;
	padding: 0.25em 0.5em;
	line-height: 1.5;
	min-height: 1.5em;
	text-align: left;
}

th {
	color: rgba(55, 53, 47, 0.6);
}

ol,
ul {
	margin: 0;
	margin-block-start: 0.6em;
	margin-block-end: 0.6em;
}

li > ol:first-child,
li > ul:first-child {
	margin-block-start: 0.6em;
}

ul > li {
	list-style: disc;
}

ul.to-do-list {
	padding-inline-start: 0;
}

ul.to-do-list > li {
	list-style: none;
}

.to-do-children-checked {
	text-decoration: line-through;
	opacity: 0.375;
}

ul.toggle > li {
	list-style: none;
}

ul {
	padding-inline-start: 1.7em;
}

ul > li {
	padding-left: 0.1em;
}

ol {
	padding-inline-start: 1.6em;
}

ol > li {
	padding-left: 0.2em;
}

.mono ol {
	padding-inline-start: 2em;
}

.mono ol > li {
	text-indent: -0.4em;
}

.toggle {
	padding-inline-start: 0em;
	list-style-type: none;
}

/* Indent toggle children */
.toggle > li > details {
	padding-left: 1.7em;
}

.toggle > li > details > summary {
	margin-left: -1.1em;
}

.selected-value {
	display: inline-block;
	padding: 0 0.5em;
	background: rgba(206, 205, 202, 0.5);
	border-radius: 3px;
	margin-right: 0.5em;
	margin-top: 0.3em;
	margin-bottom: 0.3em;
	white-space: nowrap;
}

.collection-title {
	display: inline-block;
	margin-right: 1em;
}

.page-description {
    margin-bottom: 2em;
}

.simple-table {
	margin-top: 1em;
	font-size: 0.875rem;
	empty-cells: show;
}
.simple-table td {
	height: 29px;
	min-width: 120px;
}

.simple-table th {
	height: 29px;
	min-width: 120px;
}

.simple-table-header-color {
	background: rgb(247, 246, 243);
	color: black;
}
.simple-table-header {
	font-weight: 500;
}

time {
	opacity: 0.5;
}

.icon {
	display: inline-block;
	max-width: 1.2em;
	max-height: 1.2em;
	text-decoration: none;
	vertical-align: text-bottom;
	margin-right: 0.5em;
}

img.icon {
	border-radius: 3px;
}

.user-icon {
	width: 1.5em;
	height: 1.5em;
	border-radius: 100%;
	margin-right: 0.5rem;
}

.user-icon-inner {
	font-size: 0.8em;
}

.text-icon {
	border: 1px solid #000;
	text-align: center;
}

.page-cover-image {
	display: block;
	object-fit: cover;
	width: 100%;
	max-height: 30vh;
}

.page-header-icon {
	font-size: 3rem;
	margin-bottom: 1rem;
}

.page-header-icon-with-cover {
	margin-top: -0.72em;
	margin-left: 0.07em;
}

.page-header-icon img {
	border-radius: 3px;
}

.link-to-page {
	margin: 1em 0;
	padding: 0;
	border: none;
	font-weight: 500;
}

p > .user {
	opacity: 0.5;
}

td > .user,
td > time {
	white-space: nowrap;
}

input[type="checkbox"] {
	transform: scale(1.5);
	margin-right: 0.6em;
	vertical-align: middle;
}

p {
	margin-top: 0.5em;
	margin-bottom: 0.5em;
}

.image {
	border: none;
	margin: 1.5em 0;
	padding: 0;
	border-radius: 0;
	text-align: center;
}

.code,
code {
	background: rgba(135, 131, 120, 0.15);
	border-radius: 3px;
	padding: 0.2em 0.4em;
	border-radius: 3px;
	font-size: 85%;
	tab-size: 2;
}

code {
	color: #eb5757;
}

.code {
	padding: 1.5em 1em;
}

.code-wrap {
	white-space: pre-wrap;
	word-break: break-all;
}

.code > code {
	background: none;
	padding: 0;
	font-size: 100%;
	color: inherit;
}

blockquote {
	font-size: 1.25em;
	margin: 1em 0;
	padding-left: 1em;
	border-left: 3px solid rgb(55, 53, 47);
}

.bookmark {
	text-decoration: none;
	max-height: 8em;
	padding: 0;
	display: flex;
	width: 100%;
	align-items: stretch;
}

.bookmark-title {
	font-size: 0.85em;
	overflow: hidden;
	text-overflow: ellipsis;
	height: 1.75em;
	white-space: nowrap;
}

.bookmark-text {
	display: flex;
	flex-direction: column;
}

.bookmark-info {
	flex: 4 1 180px;
	padding: 12px 14px 14px;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.bookmark-image {
	width: 33%;
	flex: 1 1 180px;
	display: block;
	position: relative;
	object-fit: cover;
	border-radius: 1px;
}

.bookmark-description {
	color: rgba(55, 53, 47, 0.6);
	font-size: 0.75em;
	overflow: hidden;
	max-height: 4.5em;
	word-break: break-word;
}

.bookmark-href {
	font-size: 0.75em;
	margin-top: 0.25em;
}

.sans { font-family: ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol"; }
.code { font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace; }
.serif { font-family: Lyon-Text, Georgia, ui-serif, serif; }
.mono { font-family: iawriter-mono, Nitti, Menlo, Courier, monospace; }
.pdf .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK JP''; }
.pdf:lang(zh-CN) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK SC''; }
.pdf:lang(zh-TW) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK TC''; }
.pdf:lang(ko-KR) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK KR''; }
.pdf .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK JP''; }
.pdf:lang(zh-CN) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK SC''; }
.pdf:lang(zh-TW) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK TC''; }
.pdf:lang(ko-KR) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK KR''; }
.pdf .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK JP''; }
.pdf:lang(zh-CN) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK SC''; }
.pdf:lang(zh-TW) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK TC''; }
.pdf:lang(ko-KR) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK KR''; }
.pdf .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK JP''; }
.pdf:lang(zh-CN) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK SC''; }
.pdf:lang(zh-TW) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK TC''; }
.pdf:lang(ko-KR) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK KR''; }
.highlight-default {
	color: rgba(55, 53, 47, 1);
}
.highlight-gray {
	color: rgba(120, 119, 116, 1);
	fill: rgba(120, 119, 116, 1);
}
.highlight-brown {
	color: rgba(159, 107, 83, 1);
	fill: rgba(159, 107, 83, 1);
}
.highlight-orange {
	color: rgba(217, 115, 13, 1);
	fill: rgba(217, 115, 13, 1);
}
.highlight-yellow {
	color: rgba(203, 145, 47, 1);
	fill: rgba(203, 145, 47, 1);
}
.highlight-teal {
	color: rgba(68, 131, 97, 1);
	fill: rgba(68, 131, 97, 1);
}
.highlight-blue {
	color: rgba(51, 126, 169, 1);
	fill: rgba(51, 126, 169, 1);
}
.highlight-purple {
	color: rgba(144, 101, 176, 1);
	fill: rgba(144, 101, 176, 1);
}
.highlight-pink {
	color: rgba(193, 76, 138, 1);
	fill: rgba(193, 76, 138, 1);
}
.highlight-red {
	color: rgba(212, 76, 71, 1);
	fill: rgba(212, 76, 71, 1);
}
.highlight-default_background {
	color: rgba(55, 53, 47, 1);
}
.highlight-gray_background {
	background: rgba(241, 241, 239, 1);
}
.highlight-brown_background {
	background: rgba(244, 238, 238, 1);
}
.highlight-orange_background {
	background: rgba(251, 236, 221, 1);
}
.highlight-yellow_background {
	background: rgba(251, 243, 219, 1);
}
.highlight-teal_background {
	background: rgba(237, 243, 236, 1);
}
.highlight-blue_background {
	background: rgba(231, 243, 248, 1);
}
.highlight-purple_background {
	background: rgba(244, 240, 247, 0.8);
}
.highlight-pink_background {
	background: rgba(249, 238, 243, 0.8);
}
.highlight-red_background {
	background: rgba(253, 235, 236, 1);
}
.block-color-default {
	color: inherit;
	fill: inherit;
}
.block-color-gray {
	color: rgba(120, 119, 116, 1);
	fill: rgba(120, 119, 116, 1);
}
.block-color-brown {
	color: rgba(159, 107, 83, 1);
	fill: rgba(159, 107, 83, 1);
}
.block-color-orange {
	color: rgba(217, 115, 13, 1);
	fill: rgba(217, 115, 13, 1);
}
.block-color-yellow {
	color: rgba(203, 145, 47, 1);
	fill: rgba(203, 145, 47, 1);
}
.block-color-teal {
	color: rgba(68, 131, 97, 1);
	fill: rgba(68, 131, 97, 1);
}
.block-color-blue {
	color: rgba(51, 126, 169, 1);
	fill: rgba(51, 126, 169, 1);
}
.block-color-purple {
	color: rgba(144, 101, 176, 1);
	fill: rgba(144, 101, 176, 1);
}
.block-color-pink {
	color: rgba(193, 76, 138, 1);
	fill: rgba(193, 76, 138, 1);
}
.block-color-red {
	color: rgba(212, 76, 71, 1);
	fill: rgba(212, 76, 71, 1);
}
.block-color-default_background {
	color: inherit;
	fill: inherit;
}
.block-color-gray_background {
	background: rgba(241, 241, 239, 1);
}
.block-color-brown_background {
	background: rgba(244, 238, 238, 1);
}
.block-color-orange_background {
	background: rgba(251, 236, 221, 1);
}
.block-color-yellow_background {
	background: rgba(251, 243, 219, 1);
}
.block-color-teal_background {
	background: rgba(237, 243, 236, 1);
}
.block-color-blue_background {
	background: rgba(231, 243, 248, 1);
}
.block-color-purple_background {
	background: rgba(244, 240, 247, 0.8);
}
.block-color-pink_background {
	background: rgba(249, 238, 243, 0.8);
}
.block-color-red_background {
	background: rgba(253, 235, 236, 1);
}
.select-value-color-uiBlue { background-color: rgba(35, 131, 226, .07); }
.select-value-color-pink { background-color: rgba(245, 224, 233, 1); }
.select-value-color-purple { background-color: rgba(232, 222, 238, 1); }
.select-value-color-green { background-color: rgba(219, 237, 219, 1); }
.select-value-color-gray { background-color: rgba(227, 226, 224, 1); }
.select-value-color-transparentGray { background-color: rgba(227, 226, 224, 0); }
.select-value-color-translucentGray { background-color: rgba(0, 0, 0, 0.06); }
.select-value-color-orange { background-color: rgba(250, 222, 201, 1); }
.select-value-color-brown { background-color: rgba(238, 224, 218, 1); }
.select-value-color-red { background-color: rgba(255, 226, 221, 1); }
.select-value-color-yellow { background-color: rgba(253, 236, 200, 1); }
.select-value-color-blue { background-color: rgba(211, 229, 239, 1); }
.select-value-color-pageGlass { background-color: undefined; }
.select-value-color-washGlass { background-color: undefined; }

.checkbox {
	display: inline-flex;
	vertical-align: text-bottom;
	width: 16;
	height: 16;
	background-size: 16px;
	margin-left: 2px;
	margin-right: 5px;
}

.checkbox-on {
	background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20width%3D%2216%22%20height%3D%2216%22%20fill%3D%22%2358A9D7%22%2F%3E%0A%3Cpath%20d%3D%22M6.71429%2012.2852L14%204.9995L12.7143%203.71436L6.71429%209.71378L3.28571%206.2831L2%207.57092L6.71429%2012.2852Z%22%20fill%3D%22white%22%2F%3E%0A%3C%2Fsvg%3E");
}

.checkbox-off {
	background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20x%3D%220.75%22%20y%3D%220.75%22%20width%3D%2214.5%22%20height%3D%2214.5%22%20fill%3D%22white%22%20stroke%3D%22%2336352F%22%20stroke-width%3D%221.5%22%2F%3E%0A%3C%2Fsvg%3E");
}

</style></head><body><article id="14c45b57-a008-80a9-b2e0-f39c47ef9b55" class="page sans"><header><h1 class="page-title">회원가입 약관동의</h1><p class="page-description"></p></header><div class="page-body"><h1 id="3ad63c5f-66e7-4e01-a20e-98ca86050a97" class="">EM-STOCK 이용약관</h1><h2 id="9940cc44-b4da-428e-be5c-db9a379d1784" class="">1. 목적</h2><p id="9295c522-be0c-4cbb-9486-e62f1270a0b8" class="">이 서비스 이용약관(이하 &quot;약관&quot;이라 합니다)은 (주)illunex(이하 &quot;회사&quot;라 합니다)가 제공하는 주가정보 제공 서비스와 관련하여 회사와 이용고객(또는 회원) 간에 서비스의 이용 조건 및 절차, 회사와 회원 간의 권리, 의무 및 책임사항과, 기타 필요한 사항을 규정함을 목적으로 합니다.</p><h2 id="5e9db2d2-ee11-4053-8a93-f2863b653f4a" class="">2. 용어의 정의</h2><p id="eb008f85-4a7f-4183-abe3-31736fc2b126" class="">이 약관에서 사용하는 용어의 정의는 다음 각호와 같으며, 정의되지 않은 용어에 대한 해석은 관계법령 및 서비스별 안내에서 정하는 바에 따릅니다.</p><ol type="1" id="7daeeedf-3b06-48e0-b13b-4fc33c1ce1f7" class="numbered-list" start="1"><li>서비스<ul id="d471e4a6-d007-4223-8afa-72919e7397d0" class="bulleted-list"><li style="list-style-type:disc">이용 고객 또는 회원이 PC, 모바일 기기 등 각종 전자기기를 통하여 이용할 수 있도록 회사가 제공하는 주가정보 제공 서비스를 말합니다.</li></ul><ul id="7c75b1b2-b0dc-47e2-b96b-a16d29599fa7" class="bulleted-list"><li style="list-style-type:disc">본 서비스는 투자 참고용 주가정보 제공에 한정되며, 실제 주식 거래 기능은 제공하지 않습니다.</li></ul><ul id="a3faee9b-995b-45b8-be90-b3735b253039" class="bulleted-list"><li style="list-style-type:disc">회사가 공개한 API를 이용하여 제3자가 개발 또는 구축한 프로그램이나 서비스를 통하여 이용 고객 또는 회원에게 제공되는 경우를 포함합니다.</li></ul></li></ol><ol type="1" id="89955169-dfc5-49e3-a87b-2026492cae86" class="numbered-list" start="2"><li>회원</li></ol><ul id="b2d1bc0a-0257-4093-b181-faf39902e048" class="bulleted-list"><li style="list-style-type:disc">서비스에 접속하여 이 약관에 동의하고 ID와 비밀번호를 발급 받은 이용고객을 말합니다.</li></ul><ol type="1" id="f37d9571-5f5d-4066-816a-3e45841fac4a" class="numbered-list" start="1"><li>이용고객<ul id="37878c2b-346a-424a-bc2c-5a2fb25baaa1" class="bulleted-list"><li style="list-style-type:disc">서비스를 이용하기 위하여 회사와 이용계약을 체결하려고 하는 자를 말합니다.</li></ul></li></ol><ol type="1" id="9e9daa8b-f0d0-475a-8e1c-8d5c819feac9" class="numbered-list" start="2"><li>회원정보<ul id="05347205-1d10-4d98-b13d-b52bff379c44" class="bulleted-list"><li style="list-style-type:disc">성명, 연락처 등 회사가 이용고객에게 회원가입 신청양식(이하 &quot;신청양식&quot;이라 합니다)에 기재를 요청하는 이용고객의 개인정보를 말합니다.</li></ul></li></ol><ol type="1" id="409b8ddb-6736-48a6-b1ab-25a44cc6bc62" class="numbered-list" start="3"><li>ID(고유번호)<ul id="73c9e5ca-09ac-482c-bb64-f8f1d60af52f" class="bulleted-list"><li style="list-style-type:disc">회원의 식별과 회원의 서비스 이용을 위하여 회원이 선정하고 회사가 승인하는 영문자와 숫자의 조합을 말합니다.</li></ul></li></ol><ol type="1" id="94105fc4-34f6-46e2-92f1-7a2071df95d6" class="numbered-list" start="4"><li>비밀번호<ul id="b5c89019-d193-4a13-87ee-53aa7a9cf94c" class="bulleted-list"><li style="list-style-type:disc">회원의 회원정보 보호를 위해 회원 자신이 설정한 문자와 숫자의 조합을 말합니다.</li></ul></li></ol><ol type="1" id="8dbd4919-796f-4f75-a802-e6bc6dcc9d41" class="numbered-list" start="5"><li>게시물<ul id="fed2751e-9dba-42d4-804b-34f065b248ae" class="bulleted-list"><li style="list-style-type:disc">회원이 회사가 제공하는 서비스에 게시 또는 등록하는 부호(URL 포함), 문자, 이미지(사진 포함), 파일 등을 말합니다.</li></ul></li></ol><h2 id="bc76196e-33b9-4724-9e66-1f5e21e62e2b" class="">3. 서비스의 범위 및 책임</h2><ol type="1" id="22f83dcc-f128-40cf-b7f0-f43b31bfd956" class="numbered-list" start="1"><li>본 서비스는 투자 참고용 주가정보 제공에 한정되며, 주식 매매 중개 또는 투자 중개 서비스는 제공하지 않습니다.</li></ol><ol type="1" id="c33dd243-a83f-4a48-97ee-a0e03df56f71" class="numbered-list" start="2"><li>서비스를 통해 제공되는 모든 정보는 투자 참고사항이며, 투자 권유가 아닙니다.</li></ol><ol type="1" id="4a2f3ec9-aafa-4c99-9d1e-7e36fe389221" class="numbered-list" start="3"><li>투자 의사결정은 전적으로 회원 개인의 판단에 따라 이루어져야 하며, 이에 따른 결과에 대한 책임은 전적으로 회원 본인에게 있습니다.</li></ol><ol type="1" id="e3ad48c7-2fff-43c9-a683-46bd70540e64" class="numbered-list" start="4"><li>회사는 제공하는 정보의 정확성과 신뢰성을 위해 최선을 다하나, 제공된 정보의 지연, 오류, 누락이 발생할 수 있습니다.</li></ol><h2 id="de3d4a0e-8667-4a2c-b15a-8a524fa4a532" class="">4. 정보의 제공 및 이용</h2><ol type="1" id="af13421d-f53a-4834-b95c-18e98d1c833f" class="numbered-list" start="1"><li>회사는 다음과 같은 주가정보를 제공합니다:<ul id="cb2b2192-c696-4353-a01f-f673b56b0d30" class="bulleted-list"><li style="list-style-type:disc">국내 주식시장 시세 정보</li></ul><ul id="968aa28f-9c9c-4d31-9ece-f6076d28129d" class="bulleted-list"><li style="list-style-type:disc">기업 재무정보</li></ul><ul id="a881f0a6-b917-460d-9b67-82f9e6385b12" class="bulleted-list"><li style="list-style-type:disc">시장 분석 정보</li></ul><ul id="4a099f52-ddc2-43e5-a42e-060f9f0c3866" class="bulleted-list"><li style="list-style-type:disc">기타 투자 참고 정보</li></ul></li></ol><ol type="1" id="53e91514-8f14-41ab-91fb-89cd60ec835a" class="numbered-list" start="2"><li>정보 제공의 시점과 범위는 다음과 같습니다:<ul id="46056d71-1739-4454-a9f5-2b35d778a425" class="bulleted-list"><li style="list-style-type:disc">실시간 정보가 아닌 일정 시간 지연된 정보를 제공할 수 있습니다.</li></ul><ul id="423c990b-c70b-4511-9406-f747b485c5bd" class="bulleted-list"><li style="list-style-type:disc">정보의 업데이트는 회사가 정한 주기에 따라 이루어집니다.</li></ul><ul id="40389e8c-53db-4698-8a8a-e59065839e52" class="bulleted-list"><li style="list-style-type:disc">시장 상황이나 기술적 문제로 인해 정보 제공이 지연되거나 중단될 수 있습니다.</li></ul></li></ol><ol type="1" id="0c50d151-f606-456d-b439-c4e1b8751675" class="numbered-list" start="3"><li>회원은 제공된 정보를 다음과 같이 이용해야 합니다:<ul id="64427654-fa20-4258-9ba0-2812844a1530" class="bulleted-list"><li style="list-style-type:disc">제공된 정보는 회원의 투자 판단을 위한 참고자료로만 사용해야 합니다.</li></ul><ul id="a593fef8-5d51-4bb0-8472-15ece2a15045" class="bulleted-list"><li style="list-style-type:disc">영리목적의 정보 재판매나 재배포는 금지됩니다.</li></ul><ul id="cf79bef9-2e5f-49a7-8126-ad49dda098a2" class="bulleted-list"><li style="list-style-type:disc">타인의 투자판단에 영향을 미칠 수 있는 허위정보 배포는 금지됩니다.</li></ul></li></ol><h2 id="b22fabb0-4a8d-4316-9d00-0e9921dccbb7" class="">5. 약관의 효력 및 변경</h2><ol type="1" id="178a8149-c393-4e02-b1a2-27a8745f329d" class="numbered-list" start="1"><li>이 약관은 서비스 화면에 게시하거나 기타의 방법으로 회원에게 공지함으로써 효력이 발생합니다.</li></ol><ol type="1" id="c4be97c4-bdb3-4d49-a621-33dd5c12e72d" class="numbered-list" start="2"><li>회사는 약관의 규제에 관한 법률, 전자상거래등에서의 소비자보호에 관한 법률, 정보통신망 이용촉진 및 정보보호 등에 관한 법률 등 관계법령에 위배되지 않는 범위내에서 이 약관을 개정할 수 있습니다.</li></ol><p id="bb0796a7-6010-4612-b824-129246a5e53a" class="">
</p><h2 id="b900b525-a6ec-4d86-9ad3-ebbbfdb5d163" class="">6. 이용계약의 성립</h2><ol type="1" id="85e28c50-c2df-44e9-a2bb-caaa1ade5de9" class="numbered-list" start="1"><li>서비스 이용계약은 이용고객이 이 약관 및 &quot;개인정보취급방침&quot;에 &quot;동의합니다&quot;를 선택하고, 회사가 정한 신청양식을 작성하여 서비스의 이용을 신청한 후, 회사가 이를 승낙함으로써 성립합니다.</li></ol><ol type="1" id="027fc1e4-df72-4a3a-b873-0e1c31675010" class="numbered-list" start="2"><li>이용고객이 전항의 &quot;동의합니다&quot;를 선택하고 신청양식을 작성하는 것은 이 약관 및 &quot;개인정보취급방침&quot;의 내용을 숙지하고, 회사가 서비스 이용을 위해 운영하는 각종 정책과 수시로 공지하는 사항을 준수하는 것에 대해 동의하는 것으로 봅니다.</li></ol><h2 id="89d53f7c-c550-457e-ac60-434796f331bc" class="">7. 이용신청 및 승낙</h2><ol type="1" id="0ab56a8f-26aa-4858-ba73-051b91515244" class="numbered-list" start="1"><li>회원이 신청양식에 기재하는 회원정보는 이용고객의 실제정보인 것으로 간주되고, 실제정보를 입력하지 않은 회원은 법적인 보호를 받을 수 없으며 서비스 이용에 제한을 받을 수 있습니다.</li></ol><ol type="1" id="dc3464f1-1325-4624-9928-2a7e3fef53ed" class="numbered-list" start="2"><li>만14세 미만의 아동이 서비스를 이용하기 위해서는 회사가 요구하는 소정의 법정대리인의 동의절차를 거쳐야 합니다.</li></ol><ol type="1" id="4eaf2156-4b32-461f-9ea5-4c3abc2a542f" class="numbered-list" start="3"><li>회사는 다음 각 호에 해당하는 이용신청에 대하여는 승낙을 하지 않을 수 있습니다:<ul id="9e281053-8b9a-48ae-aebb-6150ba7e8cfd" class="bulleted-list"><li style="list-style-type:disc">기술상 서비스 제공이 불가능한 경우</li></ul><ul id="57c89bd1-5de9-4c09-950f-57cb202bfeb8" class="bulleted-list"><li style="list-style-type:disc">신청양식을 허위로 기재한 경우</li></ul><ul id="4336c22e-105c-4eda-8181-556c7bc2ede1" class="bulleted-list"><li style="list-style-type:disc">신청양식의 기재사항을 누락하거나 오기하여 신청하는 경우</li></ul><ul id="877a35ff-054c-4eef-96b9-b7b35c79fc2a" class="bulleted-list"><li style="list-style-type:disc">회사의 안녕질서 또는 미풍양속을 저해하거나 저해할 목적으로 신청한 경우</li></ul><ul id="dbca6c18-1034-46c7-b2e1-810064c861fa" class="bulleted-list"><li style="list-style-type:disc">다른 회원의 ID를 도용하거나 부정한 목적으로 서비스를 신청하는 경우</li></ul></li></ol><h2 id="e562e1a8-e5ef-43c7-a483-48ffdd3f87e9" class="">8. 서비스 이용료</h2><ol type="1" id="f9eb4df6-997d-41f8-99be-b3a8af2278c0" class="numbered-list" start="1"><li>기본적인 주가정보 제공 서비스는 무료로 제공됩니다.</li></ol><ol type="1" id="ce8896a3-ce98-4d1b-8dc7-d5a3c803ed24" class="numbered-list" start="2"><li>회사는 특정 정보나 서비스에 대해 유료로 제공할 수 있으며, 이 경우 해당 서비스의 이용요금과 결제 방식을 별도로 공지합니다.</li></ol><ol type="1" id="86b3fc4b-c0f8-4904-be3b-347f25f7849f" class="numbered-list" start="3"><li>유료 서비스의 경우, 관련 법령에서 정한 바에 따라 환불이 가능합니다.</li></ol><h2 id="41808725-510c-48a6-8b8f-71454dce6f45" class="">9. 회원의 의무</h2><ol type="1" id="4eca124a-626c-43b3-bec0-3fe6afcdb18e" class="numbered-list" start="1"><li>회원은 관계법령, 본 약관의 규정, 이용안내 및 서비스와 관련하여 공지한 주의사항, 회사가 통지하는 사항 등을 준수하여야 하며, 기타 회사의 업무에 방해되는 행위를 하여서는 안됩니다.</li></ol><ol type="1" id="4f6003a0-00ad-45bf-b73c-cf971a681221" class="numbered-list" start="2"><li>회원은 다음 각 호의 행위를 해서는 안됩니다:<ul id="84bd4b77-00ac-4f32-a74f-fc25ecd72888" class="bulleted-list"><li style="list-style-type:disc">제공된 정보의 무단 복제, 배포, 방송 또는 전송행위</li></ul><ul id="5482fc34-3188-4a88-b325-c6864104f3b6" class="bulleted-list"><li style="list-style-type:disc">부정한 방법으로 서비스를 이용하는 행위</li></ul><ul id="18955198-fc8e-4ce2-b472-b80cc677211b" class="bulleted-list"><li style="list-style-type:disc">회사의 저작권, 제3자의 저작권 등 기타 권리를 침해하는 행위</li></ul><ul id="66ff1a71-6f38-42e0-9325-249da260d826" class="bulleted-list"><li style="list-style-type:disc">서비스를 이용하여 허위정보를 유포하는 행위</li></ul><ul id="45692a1f-8e7f-4ec3-860f-78b827f01cf8" class="bulleted-list"><li style="list-style-type:disc">제3자의 투자판단에 영향을 미칠 수 있는 허위, 조작정보를 유포하는 행위</li></ul></li></ol><h2 id="dc798032-e219-4f43-8270-916da7f7f1d0" class="">10. 회사의 의무</h2><ol type="1" id="dd9108e8-100e-428a-9013-57a031f84dff" class="numbered-list" start="1"><li>회사는 관계법령과 본 약관이 금지하는 행위를 하지 않으며, 계속적이고 안정적으로 서비스를 제공하기 위하여 최선을 다하여야 합니다.</li></ol><ol type="1" id="985267cd-3f86-42c8-a922-7b3318c3492e" class="numbered-list" start="2"><li>회사는 회원이 안전하게 서비스를 이용할 수 있도록 개인정보(신용정보 포함) 보호를 위해 보안시스템을 갖추어야 하며, 개인정보취급방침을 공시하고 준수합니다.</li></ol><ol type="1" id="10b75f1c-09e6-410f-87f4-e46a9e107dc3" class="numbered-list" start="3"><li>회사는 서비스 이용과 관련하여 회원으로부터 제기된 의견이나 불만이 정당하다고 인정할 경우에는 이를 처리하여야 합니다.</li></ol><h2 id="28143da1-0321-485b-a6aa-0f92c601ed97" class="">11. 손해배상 및 면책</h2><ol type="1" id="d0ac7b25-c895-48fc-bb78-7ebd23e139ca" class="numbered-list" start="1"><li>회사는 서비스 제공과 관련하여 다음과 같은 경우에는 책임을 지지 않습니다:<ul id="24eb28bd-1faa-4092-acd8-e0a783c7fb2d" class="bulleted-list"><li style="list-style-type:disc">제공된 투자 정보를 기반으로 한 투자 결과에 대한 책임</li></ul><ul id="3ef4fca4-f22d-4916-80d0-9216e419ae8c" class="bulleted-list"><li style="list-style-type:disc">회원 간의 정보 교류를 통해 발생한 분쟁에 대한 책임</li></ul><ul id="42ddda72-35c2-4aca-84b4-efc3d527addb" class="bulleted-list"><li style="list-style-type:disc">천재지변, 전쟁, 기간통신사업자의 서비스 중지 등 불가항력적인 사유로 인한 서비스 중단</li></ul><ul id="1194256e-bb8e-4a55-b9ea-7b23e48caabc" class="bulleted-list"><li style="list-style-type:disc">회원의 귀책사유로 인한 서비스 이용의 장애</li></ul></li></ol><ol type="1" id="0ace9b86-2a84-4bb0-a8e8-b92e3cd0ca1f" class="numbered-list" start="2"><li>회사는 제공하는 정보의 정확성과 신뢰성을 위해 최선을 다하나, 정보의 오류, 지연, 누락으로 인한 손해에 대해서는 책임을 지지 않습니다.</li></ol><h2 id="74b64174-0766-4f26-be55-d8cda0f4185d" class="">12. 분쟁해결</h2><ol type="1" id="af0eded1-664c-4d0d-8fff-09bd9702b96f" class="numbered-list" start="1"><li>서비스 이용과 관련하여 회사와 회원 사이에 분쟁이 발생한 경우, 회사와 회원은 분쟁의 해결을 위해 성실히 협의합니다.</li></ol><ol type="1" id="2b0810a1-21de-420a-8e95-965728acdbc8" class="numbered-list" start="2"><li>본 약관은 대한민국 법률에 따라 규율되고 해석되며, 회사와 회원간의 분쟁에 대한 소송은 관할법원에 제기합니다.</li></ol></div></article><span class="sans" style="font-size:14px;padding-top:2em"></span></body></html>',
     true, true, now()),
    (2, '개인정보 이용약관', '<html><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8"/><title>개인정보 이용 약관</title><style>
/* cspell:disable-file */
/* webkit printing magic: print all background colors */
html {
	-webkit-print-color-adjust: exact;
}
* {
	box-sizing: border-box;
	-webkit-print-color-adjust: exact;
}

html,
body {
	margin: 0;
	padding: 0;
}
@media only screen {
	body {
		margin: 2em auto;
		max-width: 900px;
		color: rgb(55, 53, 47);
	}
}

body {
	line-height: 1.5;
	white-space: pre-wrap;
}

a,
a.visited {
	color: inherit;
	text-decoration: underline;
}

.pdf-relative-link-path {
	font-size: 80%;
	color: #444;
}

h1,
h2,
h3 {
	letter-spacing: -0.01em;
	line-height: 1.2;
	font-weight: 600;
	margin-bottom: 0;
}

.page-title {
	font-size: 2.5rem;
	font-weight: 700;
	margin-top: 0;
	margin-bottom: 0.75em;
}

h1 {
	font-size: 1.875rem;
	margin-top: 1.875rem;
}

h2 {
	font-size: 1.5rem;
	margin-top: 1.5rem;
}

h3 {
	font-size: 1.25rem;
	margin-top: 1.25rem;
}

.source {
	border: 1px solid #ddd;
	border-radius: 3px;
	padding: 1.5em;
	word-break: break-all;
}

.callout {
	border-radius: 3px;
	padding: 1rem;
}

figure {
	margin: 1.25em 0;
	page-break-inside: avoid;
}

figcaption {
	opacity: 0.5;
	font-size: 85%;
	margin-top: 0.5em;
}

mark {
	background-color: transparent;
}

.indented {
	padding-left: 1.5em;
}

hr {
	background: transparent;
	display: block;
	width: 100%;
	height: 1px;
	visibility: visible;
	border: none;
	border-bottom: 1px solid rgba(55, 53, 47, 0.09);
}

img {
	max-width: 100%;
}

@media only print {
	img {
		max-height: 100vh;
		object-fit: contain;
	}
}

@page {
	margin: 1in;
}

.collection-content {
	font-size: 0.875rem;
}

.column-list {
	display: flex;
	justify-content: space-between;
}

.column {
	padding: 0 1em;
}

.column:first-child {
	padding-left: 0;
}

.column:last-child {
	padding-right: 0;
}

.table_of_contents-item {
	display: block;
	font-size: 0.875rem;
	line-height: 1.3;
	padding: 0.125rem;
}

.table_of_contents-indent-1 {
	margin-left: 1.5rem;
}

.table_of_contents-indent-2 {
	margin-left: 3rem;
}

.table_of_contents-indent-3 {
	margin-left: 4.5rem;
}

.table_of_contents-link {
	text-decoration: none;
	opacity: 0.7;
	border-bottom: 1px solid rgba(55, 53, 47, 0.18);
}

table,
th,
td {
	border: 1px solid rgba(55, 53, 47, 0.09);
	border-collapse: collapse;
}

table {
	border-left: none;
	border-right: none;
}

th,
td {
	font-weight: normal;
	padding: 0.25em 0.5em;
	line-height: 1.5;
	min-height: 1.5em;
	text-align: left;
}

th {
	color: rgba(55, 53, 47, 0.6);
}

ol,
ul {
	margin: 0;
	margin-block-start: 0.6em;
	margin-block-end: 0.6em;
}

li > ol:first-child,
li > ul:first-child {
	margin-block-start: 0.6em;
}

ul > li {
	list-style: disc;
}

ul.to-do-list {
	padding-inline-start: 0;
}

ul.to-do-list > li {
	list-style: none;
}

.to-do-children-checked {
	text-decoration: line-through;
	opacity: 0.375;
}

ul.toggle > li {
	list-style: none;
}

ul {
	padding-inline-start: 1.7em;
}

ul > li {
	padding-left: 0.1em;
}

ol {
	padding-inline-start: 1.6em;
}

ol > li {
	padding-left: 0.2em;
}

.mono ol {
	padding-inline-start: 2em;
}

.mono ol > li {
	text-indent: -0.4em;
}

.toggle {
	padding-inline-start: 0em;
	list-style-type: none;
}

/* Indent toggle children */
.toggle > li > details {
	padding-left: 1.7em;
}

.toggle > li > details > summary {
	margin-left: -1.1em;
}

.selected-value {
	display: inline-block;
	padding: 0 0.5em;
	background: rgba(206, 205, 202, 0.5);
	border-radius: 3px;
	margin-right: 0.5em;
	margin-top: 0.3em;
	margin-bottom: 0.3em;
	white-space: nowrap;
}

.collection-title {
	display: inline-block;
	margin-right: 1em;
}

.page-description {
    margin-bottom: 2em;
}

.simple-table {
	margin-top: 1em;
	font-size: 0.875rem;
	empty-cells: show;
}
.simple-table td {
	height: 29px;
	min-width: 120px;
}

.simple-table th {
	height: 29px;
	min-width: 120px;
}

.simple-table-header-color {
	background: rgb(247, 246, 243);
	color: black;
}
.simple-table-header {
	font-weight: 500;
}

time {
	opacity: 0.5;
}

.icon {
	display: inline-block;
	max-width: 1.2em;
	max-height: 1.2em;
	text-decoration: none;
	vertical-align: text-bottom;
	margin-right: 0.5em;
}

img.icon {
	border-radius: 3px;
}

.user-icon {
	width: 1.5em;
	height: 1.5em;
	border-radius: 100%;
	margin-right: 0.5rem;
}

.user-icon-inner {
	font-size: 0.8em;
}

.text-icon {
	border: 1px solid #000;
	text-align: center;
}

.page-cover-image {
	display: block;
	object-fit: cover;
	width: 100%;
	max-height: 30vh;
}

.page-header-icon {
	font-size: 3rem;
	margin-bottom: 1rem;
}

.page-header-icon-with-cover {
	margin-top: -0.72em;
	margin-left: 0.07em;
}

.page-header-icon img {
	border-radius: 3px;
}

.link-to-page {
	margin: 1em 0;
	padding: 0;
	border: none;
	font-weight: 500;
}

p > .user {
	opacity: 0.5;
}

td > .user,
td > time {
	white-space: nowrap;
}

input[type="checkbox"] {
	transform: scale(1.5);
	margin-right: 0.6em;
	vertical-align: middle;
}

p {
	margin-top: 0.5em;
	margin-bottom: 0.5em;
}

.image {
	border: none;
	margin: 1.5em 0;
	padding: 0;
	border-radius: 0;
	text-align: center;
}

.code,
code {
	background: rgba(135, 131, 120, 0.15);
	border-radius: 3px;
	padding: 0.2em 0.4em;
	border-radius: 3px;
	font-size: 85%;
	tab-size: 2;
}

code {
	color: #eb5757;
}

.code {
	padding: 1.5em 1em;
}

.code-wrap {
	white-space: pre-wrap;
	word-break: break-all;
}

.code > code {
	background: none;
	padding: 0;
	font-size: 100%;
	color: inherit;
}

blockquote {
	font-size: 1.25em;
	margin: 1em 0;
	padding-left: 1em;
	border-left: 3px solid rgb(55, 53, 47);
}

.bookmark {
	text-decoration: none;
	max-height: 8em;
	padding: 0;
	display: flex;
	width: 100%;
	align-items: stretch;
}

.bookmark-title {
	font-size: 0.85em;
	overflow: hidden;
	text-overflow: ellipsis;
	height: 1.75em;
	white-space: nowrap;
}

.bookmark-text {
	display: flex;
	flex-direction: column;
}

.bookmark-info {
	flex: 4 1 180px;
	padding: 12px 14px 14px;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.bookmark-image {
	width: 33%;
	flex: 1 1 180px;
	display: block;
	position: relative;
	object-fit: cover;
	border-radius: 1px;
}

.bookmark-description {
	color: rgba(55, 53, 47, 0.6);
	font-size: 0.75em;
	overflow: hidden;
	max-height: 4.5em;
	word-break: break-word;
}

.bookmark-href {
	font-size: 0.75em;
	margin-top: 0.25em;
}

.sans { font-family: ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol"; }
.code { font-family: "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace; }
.serif { font-family: Lyon-Text, Georgia, ui-serif, serif; }
.mono { font-family: iawriter-mono, Nitti, Menlo, Courier, monospace; }
.pdf .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK JP''; }
.pdf:lang(zh-CN) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK SC''; }
.pdf:lang(zh-TW) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK TC''; }
.pdf:lang(ko-KR) .sans { font-family: Inter, ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI Variable Display", "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol", ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans CJK KR''; }
.pdf .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK JP''; }
.pdf:lang(zh-CN) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK SC''; }
.pdf:lang(zh-TW) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK TC''; }
.pdf:lang(ko-KR) .code { font-family: Source Code Pro, "SFMono-Regular", Menlo, Consolas, "PT Mono", "Liberation Mono", Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK KR''; }
.pdf .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK JP''; }
.pdf:lang(zh-CN) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK SC''; }
.pdf:lang(zh-TW) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK TC''; }
.pdf:lang(ko-KR) .serif { font-family: PT Serif, Lyon-Text, Georgia, ui-serif, serif, ''Twemoji'', ''Noto Color Emoji'', ''Noto Serif CJK KR''; }
.pdf .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK JP''; }
.pdf:lang(zh-CN) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK SC''; }
.pdf:lang(zh-TW) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK TC''; }
.pdf:lang(ko-KR) .mono { font-family: PT Mono, iawriter-mono, Nitti, Menlo, Courier, monospace, ''Twemoji'', ''Noto Color Emoji'', ''Noto Sans Mono CJK KR''; }
.highlight-default {
	color: rgba(55, 53, 47, 1);
}
.highlight-gray {
	color: rgba(120, 119, 116, 1);
	fill: rgba(120, 119, 116, 1);
}
.highlight-brown {
	color: rgba(159, 107, 83, 1);
	fill: rgba(159, 107, 83, 1);
}
.highlight-orange {
	color: rgba(217, 115, 13, 1);
	fill: rgba(217, 115, 13, 1);
}
.highlight-yellow {
	color: rgba(203, 145, 47, 1);
	fill: rgba(203, 145, 47, 1);
}
.highlight-teal {
	color: rgba(68, 131, 97, 1);
	fill: rgba(68, 131, 97, 1);
}
.highlight-blue {
	color: rgba(51, 126, 169, 1);
	fill: rgba(51, 126, 169, 1);
}
.highlight-purple {
	color: rgba(144, 101, 176, 1);
	fill: rgba(144, 101, 176, 1);
}
.highlight-pink {
	color: rgba(193, 76, 138, 1);
	fill: rgba(193, 76, 138, 1);
}
.highlight-red {
	color: rgba(212, 76, 71, 1);
	fill: rgba(212, 76, 71, 1);
}
.highlight-default_background {
	color: rgba(55, 53, 47, 1);
}
.highlight-gray_background {
	background: rgba(241, 241, 239, 1);
}
.highlight-brown_background {
	background: rgba(244, 238, 238, 1);
}
.highlight-orange_background {
	background: rgba(251, 236, 221, 1);
}
.highlight-yellow_background {
	background: rgba(251, 243, 219, 1);
}
.highlight-teal_background {
	background: rgba(237, 243, 236, 1);
}
.highlight-blue_background {
	background: rgba(231, 243, 248, 1);
}
.highlight-purple_background {
	background: rgba(244, 240, 247, 0.8);
}
.highlight-pink_background {
	background: rgba(249, 238, 243, 0.8);
}
.highlight-red_background {
	background: rgba(253, 235, 236, 1);
}
.block-color-default {
	color: inherit;
	fill: inherit;
}
.block-color-gray {
	color: rgba(120, 119, 116, 1);
	fill: rgba(120, 119, 116, 1);
}
.block-color-brown {
	color: rgba(159, 107, 83, 1);
	fill: rgba(159, 107, 83, 1);
}
.block-color-orange {
	color: rgba(217, 115, 13, 1);
	fill: rgba(217, 115, 13, 1);
}
.block-color-yellow {
	color: rgba(203, 145, 47, 1);
	fill: rgba(203, 145, 47, 1);
}
.block-color-teal {
	color: rgba(68, 131, 97, 1);
	fill: rgba(68, 131, 97, 1);
}
.block-color-blue {
	color: rgba(51, 126, 169, 1);
	fill: rgba(51, 126, 169, 1);
}
.block-color-purple {
	color: rgba(144, 101, 176, 1);
	fill: rgba(144, 101, 176, 1);
}
.block-color-pink {
	color: rgba(193, 76, 138, 1);
	fill: rgba(193, 76, 138, 1);
}
.block-color-red {
	color: rgba(212, 76, 71, 1);
	fill: rgba(212, 76, 71, 1);
}
.block-color-default_background {
	color: inherit;
	fill: inherit;
}
.block-color-gray_background {
	background: rgba(241, 241, 239, 1);
}
.block-color-brown_background {
	background: rgba(244, 238, 238, 1);
}
.block-color-orange_background {
	background: rgba(251, 236, 221, 1);
}
.block-color-yellow_background {
	background: rgba(251, 243, 219, 1);
}
.block-color-teal_background {
	background: rgba(237, 243, 236, 1);
}
.block-color-blue_background {
	background: rgba(231, 243, 248, 1);
}
.block-color-purple_background {
	background: rgba(244, 240, 247, 0.8);
}
.block-color-pink_background {
	background: rgba(249, 238, 243, 0.8);
}
.block-color-red_background {
	background: rgba(253, 235, 236, 1);
}
.select-value-color-uiBlue { background-color: rgba(35, 131, 226, .07); }
.select-value-color-pink { background-color: rgba(245, 224, 233, 1); }
.select-value-color-purple { background-color: rgba(232, 222, 238, 1); }
.select-value-color-green { background-color: rgba(219, 237, 219, 1); }
.select-value-color-gray { background-color: rgba(227, 226, 224, 1); }
.select-value-color-transparentGray { background-color: rgba(227, 226, 224, 0); }
.select-value-color-translucentGray { background-color: rgba(0, 0, 0, 0.06); }
.select-value-color-orange { background-color: rgba(250, 222, 201, 1); }
.select-value-color-brown { background-color: rgba(238, 224, 218, 1); }
.select-value-color-red { background-color: rgba(255, 226, 221, 1); }
.select-value-color-yellow { background-color: rgba(253, 236, 200, 1); }
.select-value-color-blue { background-color: rgba(211, 229, 239, 1); }
.select-value-color-pageGlass { background-color: undefined; }
.select-value-color-washGlass { background-color: undefined; }

.checkbox {
	display: inline-flex;
	vertical-align: text-bottom;
	width: 16;
	height: 16;
	background-size: 16px;
	margin-left: 2px;
	margin-right: 5px;
}

.checkbox-on {
	background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20width%3D%2216%22%20height%3D%2216%22%20fill%3D%22%2358A9D7%22%2F%3E%0A%3Cpath%20d%3D%22M6.71429%2012.2852L14%204.9995L12.7143%203.71436L6.71429%209.71378L3.28571%206.2831L2%207.57092L6.71429%2012.2852Z%22%20fill%3D%22white%22%2F%3E%0A%3C%2Fsvg%3E");
}

.checkbox-off {
	background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg%20width%3D%2216%22%20height%3D%2216%22%20viewBox%3D%220%200%2016%2016%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%0A%3Crect%20x%3D%220.75%22%20y%3D%220.75%22%20width%3D%2214.5%22%20height%3D%2214.5%22%20fill%3D%22white%22%20stroke%3D%22%2336352F%22%20stroke-width%3D%221.5%22%2F%3E%0A%3C%2Fsvg%3E");
}

</style></head><body><article id="3d6e069f-9523-48ad-b578-89fbd8dc553c" class="page sans"><header><h1 class="page-title">개인정보 이용 약관</h1><p class="page-description"></p></header><div class="page-body"><h1 id="34d23d32-8128-4e44-a1aa-0cf52c9f66cf" class="">[ 개인정보 수집∙이용 동의 ]</h1><p id="7508674c-8b50-4183-a9fc-df8583589ac5" class="">본인은 (주)illunex(이하 &#x27;회사&#x27;라 함)가 제공하는 주가정보 제공 서비스를 이용하기 위하여, 다음과 같이 &#x27;회사&#x27;가 본인의 개인정보를 수집∙이용하는데 동의합니다.</p><h2 id="b2da3fc1-26d2-464d-beae-3784b7e29506" class="">개인정보의 수집·이용 목적</h2><ul id="9f33e634-a9e5-47a3-bbbf-f191584316a5" class="bulleted-list"><li style="list-style-type:disc">회원가입 및 본인확인</li></ul><ul id="aa39c93b-8f26-46e0-b3dc-c9f7aa349629" class="bulleted-list"><li style="list-style-type:disc">서비스 제공 및 관리</li></ul><ul id="af4efb5e-c36f-4910-acef-d54fdb2afa1e" class="bulleted-list"><li style="list-style-type:disc">서비스 이용 분석 및 통계</li></ul><h2 id="a1448de6-0574-4b23-aa47-7782e5aa18aa" class="">수집하려는 개인정보의 항목</h2><ul id="39d3e3f1-4bf5-406c-ad39-4fe3a22bbd85" class="bulleted-list"><li style="list-style-type:disc">가. 필수 개인정보<ul id="b9034423-9675-49a4-9940-b9b89a353e1c" class="bulleted-list"><li style="list-style-type:circle">이메일 주소</li></ul><ul id="c6804941-3f14-46da-a2fc-91afbb171d63" class="bulleted-list"><li style="list-style-type:circle">비밀번호(암호화 저장)</li></ul></li></ul><ul id="d6829394-419d-4c0b-b81b-3ac2d347231b" class="bulleted-list"><li style="list-style-type:disc">나. 자동 수집 정보<ul id="7c8b7eef-5fe3-4177-aec0-804cd5ca6dbd" class="bulleted-list"><li style="list-style-type:circle">접속 기기 정보(기기 종류, OS 등)</li></ul><ul id="4f7dbb00-b097-4454-9498-ca9a86a08326" class="bulleted-list"><li style="list-style-type:circle">브라우저 종류</li></ul><ul id="b2148d2b-117b-480b-a1cf-857ec64fb542" class="bulleted-list"><li style="list-style-type:circle">방문 일시</li></ul><ul id="45590708-12bd-406d-a67a-494e888548ec" class="bulleted-list"><li style="list-style-type:circle">서비스 이용 기록</li></ul><ul id="68db6d73-3228-4f6f-8601-b0122d40a9a6" class="bulleted-list"><li style="list-style-type:circle">IP 주소</li></ul><ul id="c9e29132-c740-4f95-93a4-6c1b367edd97" class="bulleted-list"><li style="list-style-type:circle">세션 관리 정보</li></ul><ul id="c9ee1b4a-e4cf-43de-9d0c-421fcc08466f" class="bulleted-list"><li style="list-style-type:circle">사용자 설정 정보</li></ul></li></ul><h2 id="b9c0cb78-e719-4688-8ae9-3e4527f96f43" class="">개인정보의 보유 및 이용 기간</h2><p id="fcbf3372-6ce0-4677-a78c-fab5a6c3f125" class="">본인의 개인정보는 원칙적으로 개인정보의 수집 및 이용목적이 달성되면 지체없이 파기합니다. 단, 다음의 정보에 대해서는 아래의 이유로 명시한 기간 동안 보유합니다.</p><ul id="cbb6aef8-d451-4d7a-b1e4-30dd5ca25a36" class="bulleted-list"><li style="list-style-type:disc">가. 회사 내부 방침에 의한 정보보유 사유<ul id="2dcbd2d1-d21f-4292-b2c8-f457097eb918" class="bulleted-list"><li style="list-style-type:circle">회원가입 및 서비스 이용 기록</li></ul><ul id="c3e70558-c177-40e7-933a-c1e7fecd659c" class="bulleted-list"><li style="list-style-type:circle">보유 이유: 서비스 이용 관리 및 민원 처리</li></ul><ul id="95806b73-7e50-4255-8723-c2212bfc5d6f" class="bulleted-list"><li style="list-style-type:circle">보유 기간: 회원 탈퇴 시까지</li></ul></li></ul><ul id="dd817ad9-9880-4394-b4cf-f688ce92a95f" class="bulleted-list"><li style="list-style-type:disc">나. 관계법령에 의한 정보보유 사유<ul id="0d33853d-83a9-4a17-a257-ce1e19897da3" class="bulleted-list"><li style="list-style-type:circle">구독서비스 이용 기록 및 대금결제 기록</li></ul><ul id="13c6c7d5-1a2c-4750-859e-a2e3cc235cbc" class="bulleted-list"><li style="list-style-type:circle">보유 이유: 전자상거래 등에서의 소비자보호에 관한 법률</li></ul><ul id="73485e28-8bf2-4a99-a850-1b65b85dbb86" class="bulleted-list"><li style="list-style-type:circle">보유 기간: 5년</li></ul></li></ul><h2 id="c9588da9-1243-4c2c-81de-5ddad2a11936" class="">동의 거부권 및 거부 시 불이익</h2><p id="4c932d33-f782-4e69-8408-3b1e59ce4a17" class="">본인은 동의를 거부할 권리가 있으며, 동의하지 않으실 경우 회원가입 및 서비스 이용이 제한됩니다.</p><p id="4ff1a672-02e2-4d0c-8fcb-58587f035aa5" class="">
</p></div></article><span class="sans" style="font-size:14px;padding-top:2em"></span></body></html>',
     true, true, now());
