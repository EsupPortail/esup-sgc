@REM
@REM Licensed to EsupPortail under one or more contributor license
@REM agreements. See the NOTICE file distributed with this work for
@REM additional information regarding copyright ownership.
@REM
@REM EsupPortail licenses this file to you under the Apache License,
@REM Version 2.0 (the "License"); you may not use this file except in
@REM compliance with the License. You may obtain a copy of the License at:
@REM
@REM http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

openssl rsa -inform PEM -in paybox-pubkey.pem -outform DER -pubin -out paybox-pubkey.der
