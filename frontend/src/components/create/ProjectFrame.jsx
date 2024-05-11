import { useState, useEffect, useRef } from "react";
import styles from "./ProjectFrame.module.css";
import InputBox from "../common/InputBox";
import DescBox from "../common/DescBox";
import ProjectTopCreate from "../common/ProjectTopCreate";
import githubImage from "../../assets/github.png";
import gitlabImage from "../../assets/gitlab.png";
import useProjectStore from "../../stores/projectStore";


export default function ProjectFrame() {

    // const [url, setUrl] = useState(null);
    // const [accessToken, setAccessToken] = useState(null);
    // const [webHook, setWebHook] = useState(null);
    // const [branch, setBranch] = useState(null);

    const [gittype, setGittype] = useState("gitlab");
    const {createdProject, setCreatedProject} = useProjectStore();
    const [tempProject, setTempProject] = useState({...createdProject});

    const changeUrlHandler = (e) => {
        setTempProject(prev => ({
            ...prev,
            git: {
                ...prev.git,
                gitUrl: e.target.value
            }
        }));
    }

    const changeTokenHandler = (e) => {
        setTempProject(prev => ({
            ...prev,
            git: {
                ...prev.git,
                accessToken: e.target.value
            }
        }));
    }

    // 파일첨부
    const [files, setFiles] = useState([]);
    const [paths, setPaths] = useState([]);

    const handleFileChange = (event) => {
        const newFiles = Array.from(event.target.files);
        setFiles(prevFiles => [...prevFiles, ...newFiles]);
        setPaths(prevPaths => [...prevPaths, ...newFiles.map(() => "")]); // 새 파일에 대한 빈 경로 추가
    };

    const handlePathChange = (index, event) => {
        const newPaths = [...paths];
        newPaths[index] = event.target.value;
        setPaths(newPaths);
    };

    const handleUpload = async () => {
        const formData = new FormData();
        files.forEach((file, index) => {
            formData.append('files', file);
            formData.append('paths', paths[index]); // 경로 정보도 함께 전송
        });

        try {
            const response = await fetch('/upload', {
                method: 'POST',
                body: formData,
            });
            const text = await response.text();
            alert(text);
            setFiles([]); // Upload 후 파일과 경로 리스트 초기화
            setPaths([]);
        } catch (error) {
            console.error('Upload error:', error);
            alert('Upload failed');
        }
    };

    const handleRemoveFile = (index) => {
        setFiles(prevFiles => prevFiles.filter((_, idx) => idx !== index));
        setPaths(prevPaths => prevPaths.filter((_, idx) => idx !== index)); // 파일과 함께 경로도 삭제
    };

    const printFileNames = () => {
        console.log("Selected files:");
        files.forEach(file => {
            console.log(file.name);
        });
        paths.forEach(path => {
            console.log(path);
        });
    };

    const fileInputRef = useRef();
    const handleClick = () => {
        fileInputRef.current.click();
    };

    // 파일첨부


    useEffect(() => {
        setCreatedProject(tempProject);
    }, [tempProject])


    return (
        <div className={styles.page}>
            {/* <ProjectTopCreate /> */}
            {/* <GetBox keyName={"url"} valueName={url} onChange={(e) => setUrl(e.target.value)}/>
            <GetBox keyName={"액세스 토큰"} valueName={accessToken} onChange={(e)=> setAccessToken(e.target.value)}/>
            <GetBox keyName={"자동 배포 웹훅 설정"} valueName={webHook} onChange={(e)=> setWebHook(e.target.value)}/>
            <GetBox keyName={"브랜치"} valueName={branch} onChange={(e)=>setBranch(e.target.value)}/> */}
            <InputBox keyName={"프로젝트명"} valueName={"S10P31B101"} onChange={changeUrlHandler} value={tempProject.git.gitUrl} />
            <DescBox desc={"Git 저장소의 프로젝트 최상단 폴더명을 작성하세요"} />
            <InputBox keyName={"url"} valueName={"url"} onChange={changeUrlHandler} value={tempProject.git.gitUrl} />
            <DescBox desc={"GitLab 또는 GitHub 의 프로젝트를 클론하기 위한 URL을 등록하세요 "} />
            <InputBox keyName={"액세스 토큰"} valueName={"accessToken"} onChange={changeTokenHandler} value={tempProject.git.accessToken} />
            <DescBox desc={"Git 저장소에 접근하기 위한 엑세스 토큰을 발급하여 등록하세요 "} />

            <div className={styles.boxFrame}>
                <div className={styles.key}></div>
                <div className={styles.desc}>
                    <div className={styles.access}>
                        <div className={styles.accessinfo}>엑세스 토큰 발급 방법</div>
                        <div className={styles.selection}>
                            <img
                                src={gitlabImage}
                                alt="GitLab Logo" width="15%"
                                className={`${styles.logo} ${gittype === "gitlab" ? styles.selected : styles.unselected}`}
                                onClick={() => setGittype("gitlab")}
                            />
                            <img
                                src={githubImage}
                                alt="GitHub Logo" width="15%"
                                className={`${styles.logo} ${gittype === "github" ? styles.selected : styles.unselected}`}
                                onClick={() => setGittype("github")}
                            />
                        </div>
                    </div>

                    {gittype === "gitlab" && (
                        <div className={styles.gitlab}>
                            <p>1. GitHub의 프로필에서 Settings &gt; Developer Settings</p>
                            <p>2. Personal access tokens &gt; Tokens (Classic) 에서 Generate New Token 클릭</p>
                            <p>3. Note에 해당 토큰 설명 작성 (선택)</p>
                            <p>4. Expiration 은 해당 토큰 유효기간 설정</p>
                            <p>5. repo 체크 한 뒤 Generate Token 클릭</p>
                            <p>6. 발급 받은 토큰 복사해서 등록하기</p>
                        </div>
                    )}
                    {gittype === "github" && (
                        <div className={styles.github}>
                            <p>1. GitHub의 프로필에서 Settings &gt; Developer Settings</p>
                            <p>2. Personal access tokens &gt; Tokens (Classic) 에서 Generate New Token 클릭</p>
                            <p>3. Note에 해당 토큰 설명 작성 (선택)</p>
                        </div>
                    )}
                </div>
            </div>

            <div>

            </div>

            <InputBox keyName={"브랜치"} valueName={"main"} />
            <DescBox desc={"서버에 반영할 브랜치명을 입력하세요 ('main' or 'master' or 임의의 브랜치) "} />

            <div className={styles.fileFrame}>
                <div className={styles.fileTop}>
                    <div className={styles.key}>.gitignore 파일 추가</div>
                    <div className={styles.desc}>
                        {/* <input type="file" multiple onChange={handleFileChange} /> */}
                        {/* <button onClick={handleUpload}>Upload</button> */}
                        {/* <button onClick={printFileNames}>Print File Names</button> */}
                        <input
                            type="file"
                            multiple
                            ref={fileInputRef}
                            onChange={handleFileChange}
                            style={{ display: 'none' }} // 숨김 처리
                        />
                        <div className={styles.addfileButton} onClick={handleClick}>파일 선택</div>
                    </div>
                </div>

                <div className={styles.fileBottom}>
                    <div className={styles.key}></div>
                    <div className={styles.desc}>
                        <div>
                            {files.map((file, index) => (
                                <div className={styles.file} key={index}>
                                    <input
                                        className={styles.filePath}
                                        type="text"
                                        placeholder="/backend/src/main/resources"
                                        value={paths[index]}
                                        onChange={(event) => handlePathChange(index, event)} />
                                    {/* <input className={styles.value} type="text" placeholder="/backend/src/main/resources" value={paths[index]}/> */}
                                    <div className={styles.fileName}>{file.name}</div>

                                    <div className={styles.fileRemove} onClick={() => handleRemoveFile(index)} >X</div>
                                </div>
                            ))}

                        </div>
                    </div>
                </div>
            </div>
            <DescBox desc={".gitignore에 등록한 파일을 첨부하고 프로젝트 루트 경로로부터 해당 파일의 경로를 작성해주세요"} />

            <div className={styles.whiteSpace}></div>

        </div>
    );
}