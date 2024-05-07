import { useNavigate } from "react-router-dom";
import styles from "./NavLeft.module.css";
import s from "classnames";
import mainBtn from "../../assets/btn_main.png";

export default function NavLeft({ num }){
    const navigate = useNavigate();
    return(
        <div className={s(styles.container)}>
            <div className={styles.list}>
                {num === 1 ? (
                    <p className={styles.text2} onClick={() => navigate("/manage")}>Run</p>
                ) : (
                    <p className={styles.text} onClick={() => navigate("/manage")}>Run</p>
                )}
                {num === 2 ? (
                    <p className={styles.text2} onClick={() => navigate("/manage/project")}>Project</p>
                ) : (
                    <p className={styles.text} onClick={() => navigate("/manage/project")}>Project</p>
                )}
                {num === 3 ? (
                    <p className={styles.text2} onClick={() => navigate("/manage/backend")}>Backend</p>
                ) : (
                    <p className={styles.text} onClick={() => navigate("/manage/backend")}>Backend</p>
                )}
                {num === 4 ? (
                    <p className={styles.text2} onClick={() => navigate("/manage/frontend")}>Frontend</p>
                ) : (
                    <p className={styles.text} onClick={() => navigate("/manage/frontend")}>Frontend</p>
                )}
                {num === 5 ? (
                    <p className={styles.text2} onClick={() => navigate("/manage/database")}>DB</p>
                ) : (
                    <p className={styles.text} onClick={() => navigate("/manage/database")}>DB</p>
                )}
            </div>
            <img src={mainBtn} alt="search_icon" onClick={() => navigate("/main")} className={styles.img}/>
        </div>
    );

}