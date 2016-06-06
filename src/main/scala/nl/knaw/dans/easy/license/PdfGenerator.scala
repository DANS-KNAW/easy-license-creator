/**
 * Copyright (C) 2016 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.license

import java.io.{File, InputStream, OutputStream}

import scala.sys.process._

trait PdfGenerator {

  def createPdf(input: InputStream, output: OutputStream): ProcessBuilder
}

class WeasyPrintPdfGenerator(implicit parameters: Parameters) extends PdfGenerator {

  def createPdf(input: InputStream, output: OutputStream): ProcessBuilder = {
    def formatCommand(cmd: String)(userhost: String, privateKeyFile: File) = {
      s"ssh -oStrictHostKeyChecking=no -i $privateKeyFile $userhost $cmd"
    }

    val cmd = "weasyprint -e utf8 -f pdf - -"

    parameters.vagrant.fold(cmd)(formatCommand(cmd)) #< input #> output
  }
}