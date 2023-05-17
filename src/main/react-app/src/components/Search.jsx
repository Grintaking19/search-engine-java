import {useState} from 'react';
import MainSearchPage from './mainSearchPage';
import {SearchResultPage} from './SearchResultPage';

export default function Search() {

  const [search, setSearch] = useState('');
  const [enableSearch, setEnableSearch] = useState(false);

  console.log("search");
  return (
    <>
      {
        !enableSearch ? (
          <MainSearchPage setSearch={setSearch} setEnableSearch={setEnableSearch} />
        )
          :
          (
            <SearchResultPage search={search} />
          )
    }
    
    
    </>
  )

}