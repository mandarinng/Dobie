import { useState, useEffect } from "react";
import styles from "./ProjectFrame.module.css";
import GetBox from "../common/GetBox";
import ProjectTop from "../common/ProjectTop";

export default function DatabaseFrame(){
    return(
        <div className={styles.page}>
            <ProjectTop />
            <GetBox keyName={"프레임워크"} valueName={"http://abcdefg.com"}/>
            <GetBox keyName={"Username"} valueName={"FLKJ298J4F2IUFN9283JFLKSDJFSLD"}/>
            <GetBox keyName={"Password"} valueName={"머랑ㅁ ㅓㄹ;ㅣㅇ라ㅣㅁ"}/>
            <GetBox keyName={"내부 포트 번호"} valueName={"머랑ㅁ ㅓㄹ;ㅣㅇ라ㅣㅁ"}/>
            <GetBox keyName={"외부 포트 번호"} valueName={"머랑ㅁ ㅓㄹ;ㅣㅇ라ㅣㅁ"}/>
        </div>
    );
}